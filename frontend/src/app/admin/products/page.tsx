"use client"

import { ChangeEvent, useEffect, useRef, useState } from "react"

import Card from "@components/admin/Card"
import Loading from "@components/Loading"
import Popup from "@components/Popup"
import { useToastStore } from "@store/toastStore"
import { adminApi } from "@lib/api/adminApi"

import type {
  AdminBrand,
  AdminCategory,
  AdminCategoryAttribute,
  AdminProduct,
  AdminProductAttribute,
  AdminProductImage,
  CreateProductRequest,
  EditProductRequest,
} from "@_types/admin"
import TabButton from "@components/admin/TabButton"
import TopBar from "@components/admin/TopBar"
import { defaultPageMetadata, PageMetadata } from "@_types/page"
import BulkUpload from "@/src/app/admin/products/BulkUpload"
import { logger } from "@lib/logger"
import BottomBar from "@components/admin/BottomBar"
import ComboboxComponent from "@components/Combobox"
import ModalFooter from "@components/admin/ModalFooter"
import ModalHeader from "@components/admin/ModalHeader"
import FieldLabel from "@components/admin/FieldLabel"
import { parseIntOrNull, parseNumberOrNull } from "@lib/util"

function checkNegative(v: string): number | null {
  const value = parseNumberOrNull(v)
  if (value === null || value < 0) return null
  return value
}

const defaultProductForm = {
  name: "",
  sku: "",
  description: "",
  price: "0",
  salePrice: "0",
  stock: "0",
  brandId: "",
  categoryId: "",
  isActive: true,
  attributes: [],
}

interface ExistingImageState {
  id: number
  url: string
  fileName: string
  sortOrder: string
  altText: string | null
  removed: boolean
}

interface NewImageState {
  file: File
  previewUrl: string
  sortOrder: string
  altText: string | null
}

export default function AdminProductsPage() {
  const { show, hide } = useToastStore()
  const [activeTab, setActiveTab] = useState<"products" | "bulk">("products")
  const [products, setProducts] = useState<AdminProduct[]>([])
  const [productsPage, setProductsPage] = useState<PageMetadata>(defaultPageMetadata)
  const [loading, setLoading] = useState(false)
  const [pageIndex, setPageIndex] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [productQuery, setProductQuery] = useState("")
  const [brandQuery, setBrandQuery] = useState("")
  const [brands, setBrands] = useState<AdminBrand[]>([])
  const [categoryQuery, setCategoryQuery] = useState("")
  const [categories, setCategories] = useState<AdminCategory[]>([])
  const [selectedCategory, setSelectedCategory] = useState<AdminCategory>()
  const isFirstRef = useRef(true)

  const [productModal, setProductModal] = useState<{
    open: boolean
    mode: "create" | "edit"
    product?: AdminProduct
  }>({ open: false, mode: "create" })

  const [existingImages, setExistingImages] = useState<ExistingImageState[]>([])
  const [newImages, setNewImages] = useState<NewImageState[]>([])
  const [imagesDirty, setImagesDirty] = useState(false)

  const [customAttributes, setCustomAttributes] = useState<AdminProductAttribute[]>([])

  const [productForm, setProductForm] = useState<{
    name: string
    sku: string
    description: string
    price: string
    salePrice: string
    stock: string
    brandId: string
    categoryId: string
    isActive: boolean
    attributes: AdminProductAttribute[]
  }>(defaultProductForm)

  useEffect(() => {
    const timeout = setTimeout(() => {
      setLoading(true)
      fetchProducts(productQuery)
      setLoading(false)
    }, 300)
    return () => clearTimeout(timeout)
  }, [productQuery, pageSize, pageIndex])

  useEffect(() => {
    if (isFirstRef.current) return
    const timeout = setTimeout(() => {
      fetchBrandsByQuery(brandQuery)
    }, 300)
    return () => clearTimeout(timeout)
  }, [brandQuery])

  useEffect(() => {
    if (isFirstRef.current) return
    const timeout = setTimeout(() => {
      fetchCategoriesByQuery(categoryQuery)
    }, 300)
    return () => clearTimeout(timeout)
  }, [categoryQuery])

  useEffect(() => {
    isFirstRef.current = false
  }, [])

  useEffect(() => {
    return () => {
      newImages.forEach((i) => URL.revokeObjectURL(i.previewUrl))
    }
  }, [newImages])

  const fetchProducts = async (search: string = productQuery, page = pageIndex) => {
    if (search.startsWith("#")) {
      const productId = search.substring(1)
      if (!productId) return
      try {
        const product = await adminApi.getProduct(productId)
        setProducts([product])
        setProductsPage((p) => ({
          ...p,
          totalPages: 1,
        }))
      } catch (e) {
        setProducts([])
        show("Error loading products", "error")
        logger.d("Error fetching product by id", e)
      }
      return
    }

    try {
      const query = {
        search: search,
        page: page,
        size: pageSize,
      }
      const res = await adminApi.getProducts(query)
      setProducts(res.content)
      setProductsPage(res.page)
    } catch (e: unknown) {
      show("Error loading products", "error")
      logger.d("Error fetching products", e)
    }
  }

  const resetImageState = () => {
    setExistingImages([])
    setNewImages([])
    setImagesDirty(false)
  }

  const openCreateProduct = async () => {
    resetImageState()
    setCustomAttributes([])
    setProductModal({
      open: true,
      mode: "create",
    })
    setProductForm(defaultProductForm)
    if (brands.length === 0) {
      setBrandQuery("")
    }
    if (categories.length === 0) {
      setCategoryQuery("")
    }
  }

  const openEditProduct = async (p: AdminProduct) => {
    resetImageState()
    setProductModal({
      open: true,
      mode: "edit",
      product: p,
    })

    let category: AdminCategory | undefined
    const categoryId = p.category.id
    if (categoryId) {
      try {
        category = await adminApi.getCategory(categoryId)
        setCategories((prev) =>
          prev.some((c) => c.id === category?.id) ? prev : [category!, ...prev],
        )
        setSelectedCategory(category)
      } catch (e) {
        if (e?.status === 404) return
        logger.d(`Error fetching category with id: ${categoryId}`, e)
      }
    } else {
      setSelectedCategory(undefined)
    }

    const catAttrNames = new Set((category?.attributes ?? []).map((a) => a.attribute))
    const productAttrs = p.attributes ?? []
    const catDriven = productAttrs.filter((a) => catAttrNames.has(a.name))
    const custom = productAttrs
      .filter((a) => !catAttrNames.has(a.name))
      .map((a) => ({
        name: a.name,
        values: a.values ?? [],
      }))

    setCustomAttributes(custom)

    setProductForm({
      name: p.name ?? "",
      sku: p.sku ?? "",
      description: p.description ?? "",
      price: String(p.price ?? 0),
      salePrice: String(p.salePrice ?? 0),
      stock: String(p.stock ?? 0),
      brandId: p.brand?.id ? String(p.brand.id) : "",
      categoryId: categoryId ? String(p.category.id) : "",
      isActive: !!p.isActive,
      attributes: catDriven,
    })

    setExistingImages(
      (p.images ?? []).map((img: AdminProductImage) => ({
        id: img.id,
        url: img.url,
        fileName: img.fileName,
        sortOrder: String(img.sortOrder ?? 0),
        altText: img.altText ?? "",
        removed: false,
      })),
    )

    if (brands.length === 0) {
      await fetchBrandsByQuery("")
    }
    if (categories.length === 0) {
      await fetchCategoriesByQuery("")
    }
  }

  const addNewFiles = (files: FileList | null) => {
    if (!files) return
    const nextSortBase = Math.max(
      0,
      ...existingImages.filter((i) => !i.removed).map((i) => parseIntOrNull(i.sortOrder) ?? 0),
      ...newImages.map((i) => parseIntOrNull(i.sortOrder) ?? 0),
    )
    const added: NewImageState[] = Array.from(files).map((file, idx) => ({
      file,
      previewUrl: URL.createObjectURL(file),
      sortOrder: String(nextSortBase + idx + 1),
      altText: "",
    }))
    setNewImages((prev) => [...prev, ...added])
    setImagesDirty(true)
  }

  const removeExisting = (id: number) => {
    setExistingImages((prev) => prev.map((i) => (i.id === id ? { ...i, removed: true } : i)))
    setImagesDirty(true)
  }

  const restoreExisting = (id: number) => {
    setExistingImages((prev) => prev.map((i) => (i.id === id ? { ...i, removed: false } : i)))
    setImagesDirty(true)
  }

  const updateExisting = (
    id: number,
    patch: Partial<Pick<ExistingImageState, "sortOrder" | "altText">>,
  ) => {
    setExistingImages((prev) => prev.map((i) => (i.id === id ? { ...i, ...patch } : i)))
    setImagesDirty(true)
  }

  const removeNew = (idx: number) => {
    setNewImages((prev) => {
      URL.revokeObjectURL(prev[idx].previewUrl)
      return prev.filter((_, i) => i !== idx)
    })
  }

  const updateNew = (idx: number, patch: Partial<Pick<NewImageState, "sortOrder" | "altText">>) => {
    setNewImages((prev) => prev.map((item, i) => (i === idx ? { ...item, ...patch } : item)))
  }

  const submitProduct = async () => {
    hide()

    const name = productForm.name.trim()
    if (!name) {
      show("Product name is required", "error")
      return
    }

    const sku = productForm.sku.trim()
    if (!sku) {
      show("Product SKU is required", "error")
      return
    }

    const stock = checkNegative(productForm.stock)
    if (stock === null) {
      show(`Invalid stock: ${productForm.stock}`, "error")
      return
    }

    const price = checkNegative(productForm.price)
    if (price === null) {
      show(`Invalid price: ${productForm.price}`, "error")
      return
    }

    const salePrice = checkNegative(productForm.salePrice)
    if (salePrice === null) {
      show(`Invalid price: ${productForm.salePrice}`, "error")
      return
    }

    const brandId = parseIntOrNull(productForm.brandId)

    const categoryId = parseIntOrNull(productForm.categoryId)
    if (categoryId === null) {
      show(`Invalid categoryId: ${productForm.categoryId}`, "error")
    }

    const description = productForm.description.trim()

    const editImages = imagesDirty
      ? existingImages
          .filter((i) => !i.removed)
          .map((i) => ({
            id: i.id,
            sortOrder: parseIntOrNull(i.sortOrder) ?? 0,
            altText: i?.altText?.trim() || null,
          }))
      : undefined

    const newImageFiles = newImages.map((i) => i.file)
    const newImageMetadata = newImages.map((i) => ({
      sortOrder: parseIntOrNull(i.sortOrder) ?? 0,
      altText: i?.altText?.trim() || null,
      fileName: i.file.name,
    }))

    const customAsAttrs = customAttributes
      .filter((a) => a.name.trim())
      .map((a) => ({
        name: a.name.trim(),
        values: [a.values.join("")],
      }))
    const allAttributes = [...productForm.attributes, ...customAsAttrs]

    try {
      if (productModal.mode === "create") {
        const payload: CreateProductRequest = {
          name,
          sku,
          description: description,
          price,
          salePrice,
          stock,
          brandId,
          categoryId,
          isActive: productForm.isActive,
          images: newImageMetadata,
          attributes: allAttributes,
        }
        await adminApi.createProduct(payload, newImageFiles)
        show("Product created", "success")
      } else {
        if (!productModal.product) throw new Error("Missing product to edit")
        const id = productModal.product.id

        const payload: EditProductRequest = {
          id,
          name,
          sku,
          description: description,
          price,
          salePrice,
          stock,
          brandId,
          categoryId,
          isActive: productForm.isActive,
          attributes: allAttributes,
          ...(editImages !== undefined
            ? {
                images: editImages,
              }
            : {}),
        }

        await adminApi.updateProduct(id, payload, newImageFiles, newImageMetadata)
        show("Product updated", "success")
      }

      setProductModal({
        open: false,
        mode: "create",
      })
      resetImageState()
      await fetchProducts()
    } catch (e) {
      const detail = e?.response?.data?.detail
      if (detail) {
        show(detail, "error", 10000)
        return
      }
      show("Failed saving product", "error")
    }
  }

  const deleteProduct = async (p: AdminProduct) => {
    const ok = confirm(`Delete product "${p.name}"? This cannot be undone.`)
    if (!ok) return

    try {
      await adminApi.deleteProduct(p.id)
      show("Product deleted", "success")
      await fetchProducts()
    } catch (e: unknown) {
      show("Failed deleting product", "error")
    }
  }

  const fetchBrandsByQuery = async (value: string) => {
    if (value.startsWith("#")) {
      const brandId = value.substring(1)
      if (!brandId) return
      try {
        const brand = await adminApi.getBrand(brandId)
        setBrands([brand])
      } catch (e: unknown) {
        logger.d("Error fetching brand by id", e)
        setBrands([])
      }
      return
    }

    try {
      const { content } = await adminApi.getBrandsByText(value, 0, 5)
      setBrands(content)
    } catch (e: unknown) {
      logger.d("Error fetching brands by text query", e)
    }
  }

  const fetchCategoriesByQuery = async (value: string) => {
    if (value.startsWith("#")) {
      const categoryId = value.substring(1)
      if (!categoryId) return
      try {
        const category = await adminApi.getBrand(categoryId)
        setBrands([category])
      } catch (e) {
        setCategories([])
        if (e?.status === 404) return
        logger.d("Error fetching category by id", e)
      }
      return
    }
    try {
      const { content } = await adminApi.getCategoriesByText(value, 0, 5)
      setCategories(content)
    } catch (e: unknown) {
      logger.d("Error fetching categories by text query", e)
    }
  }

  const handleCategoryAttributeChange = (
    e: ChangeEvent<HTMLInputElement>,
    catAttr: AdminCategoryAttribute,
  ) => {
    const value = e.target.value
    setProductForm((f) => {
      const idx = f.attributes.findIndex((a) => a.name === catAttr.attribute)
      if (idx === -1) {
        return {
          ...f,
          attributes: [
            ...f.attributes,
            {
              name: catAttr.attribute,
              values: [value],
            },
          ],
        }
      }

      return {
        ...f,
        attributes: f.attributes.map((a, i) =>
          i === idx
            ? {
                ...a,
                values: [value],
              }
            : a,
        ),
      }
    })
  }

  return (
    <div className="mx-auto max-w-6xl space-y-5">
      <div className="flex flex-wrap gap-2">
        <TabButton active={activeTab === "products"} onClick={() => setActiveTab("products")}>
          Products
        </TabButton>
        <TabButton active={activeTab === "bulk"} onClick={() => setActiveTab("bulk")}>
          Bulk Upload
        </TabButton>
      </div>

      {activeTab === "products" && (
        <>
          {productsPage && (
            <TopBar
              page={productsPage}
              buttons={[
                {
                  name: "+ New Product",
                  onClick: openCreateProduct,
                },
              ]}
              onSetPageSize={setPageSize}
              onSearch={(q) => {
                setProductQuery(q)
                setPageIndex(0)
              }}
            />
          )}

          <Card title="Products">
            {loading ? (
              <div className="py-8">
                <Loading />
              </div>
            ) : products.length === 0 ? (
              <div className="py-10 text-center text-xs text-gray-400">No products found.</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-xs">
                  <thead>
                    <tr className="text-left tracking-widest text-gray-400 uppercase">
                      <th className="pr-3 pb-2">Name</th>
                      <th className="pr-3 pb-2">SKU</th>
                      <th className="pr-3 pb-2">Category</th>
                      <th className="pr-3 pb-2">Price</th>
                      <th className="pr-3 pb-2">Sale</th>
                      <th className="pr-3 pb-2">Stock</th>
                      {/*<th className="pr-3 pb-2">Active</th>*/}
                      <th className="pb-2 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {products.map((p) => (
                      <tr
                        key={p.id}
                        className="border-t border-gray-100 hover:bg-zinc-50 dark:hover:bg-zinc-800"
                      >
                        <td className="py-3 pr-3">
                          <div className="font-medium">{p.name}</div>
                        </td>
                        <td className="py-3 pr-3">{p.sku}</td>
                        <td className="py-3 pr-3">
                          {p.category?.id != null ? `#${p.category.id}` : "—"}
                        </td>
                        <td className="py-3 pr-3">€{p.price}</td>
                        <td className="py-3 pr-3">€{p.salePrice}</td>
                        <td className="py-3 pr-3">
                          <span className={p.stock < 10 ? "font-semibold text-red-400" : ""}>
                            {p.stock}
                          </span>
                        </td>
                        <td className="py-3 text-right">
                          <div className="flex justify-end gap-2">
                            <button
                              type="button"
                              onClick={() => openEditProduct(p)}
                              className="rounded-lg border border-gray-200 px-3 py-1 hover:bg-gray-50"
                            >
                              Edit
                            </button>
                            <button
                              type="button"
                              onClick={() => deleteProduct(p)}
                              className="rounded-lg border border-red-200 px-3 py-1 text-red-600 hover:bg-red-50"
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Card>

          {productsPage && productsPage.totalPages > 1 && (
            <BottomBar
              page={pageIndex}
              setPage={setPageIndex}
              totalPages={productsPage.totalPages}
            />
          )}
        </>
      )}

      {productModal.open && (
        <Popup>
          <div className="space-y-4">
            <ModalHeader
              title={productModal.mode === "create" ? "New product" : "Edit product"}
              onClose={() =>
                setProductModal({
                  open: false,
                  mode: "create",
                })
              }
            />

            <div className="grid grid-cols-2 gap-3">
              <label className="space-y-1">
                <FieldLabel>Name</FieldLabel>
                <input
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  value={productForm.name}
                  onChange={(e) =>
                    setProductForm((f) => ({
                      ...f,
                      name: e.target.value,
                    }))
                  }
                />
              </label>

              <label className="space-y-1">
                <FieldLabel>SKU</FieldLabel>
                <input
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  value={productForm.sku}
                  onChange={(e) =>
                    setProductForm((f) => ({
                      ...f,
                      sku: e.target.value,
                    }))
                  }
                />
              </label>

              <label className="col-span-2 space-y-1">
                <FieldLabel>Description</FieldLabel>
                <textarea
                  rows={3}
                  className="w-full resize-none rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  value={productForm.description}
                  onChange={(e) =>
                    setProductForm((f) => ({
                      ...f,
                      description: e.target.value,
                    }))
                  }
                />
              </label>

              <label className="space-y-1">
                <FieldLabel>Price</FieldLabel>
                <input
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  type="number"
                  value={productForm.price}
                  onChange={(e) =>
                    setProductForm((f) => ({
                      ...f,
                      price: e.target.value,
                    }))
                  }
                />
              </label>

              <label className="space-y-1">
                <FieldLabel>Sale price</FieldLabel>
                <input
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  type="number"
                  value={productForm.salePrice}
                  onChange={(e) =>
                    setProductForm((f) => ({
                      ...f,
                      salePrice: e.target.value,
                    }))
                  }
                />
              </label>

              <label className="space-y-1">
                <FieldLabel>stock</FieldLabel>
                <input
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  type="number"
                  value={productForm.stock}
                  onChange={(e) =>
                    setProductForm((f) => ({
                      ...f,
                      stock: e.target.value,
                    }))
                  }
                />
              </label>

              <label className="space-y-1">
                <FieldLabel>Active</FieldLabel>
                <select
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  value={productForm.isActive ? "true" : "false"}
                  onChange={(e) =>
                    setProductForm((f) => ({
                      ...f,
                      isActive: e.target.value === "true",
                    }))
                  }
                >
                  <option value="true">Active</option>
                  <option value="false">Inactive</option>
                </select>
              </label>

              {/* Brand Search*/}
              <label className="space-y-1">
                <FieldLabel>Brand name or #id</FieldLabel>
                <ComboboxComponent
                  options={brands.map((b) => b.name)}
                  selected={productModal?.product?.brand?.name ?? ""}
                  setSelected={(s) => {
                    const brandId = brands.find((b) => b.name === s)?.id?.toString() || ""
                    setProductForm((f) => ({
                      ...f,
                      brandId,
                    }))
                  }}
                  onQuery={(q) => setBrandQuery(q)}
                  placeholder={"Brand"}
                  loading={loading}
                />
              </label>

              {/*Category Search*/}
              <label className="space-y-1">
                <FieldLabel>Category name or #id</FieldLabel>
                <ComboboxComponent
                  options={categories.map((c) => c.name)}
                  selected={productModal?.product?.category?.name ?? ""}
                  setSelected={(s) => {
                    const category = categories.find((c) => c.name === s)
                    const categoryId = category?.id?.toString() || ""
                    setProductForm((f) => ({
                      ...f,
                      categoryId,
                    }))
                    setSelectedCategory(category)
                  }}
                  onQuery={(q) => setCategoryQuery(q)}
                  placeholder={"Category"}
                  loading={loading}
                />
              </label>

              {/*Attributes*/}
              <div className="col-span-2 space-y-3">
                <FieldLabel>Attributes</FieldLabel>

                {/* Category-defined attributes */}
                {selectedCategory?.attributes && selectedCategory.attributes.length > 0 && (
                  <div className="space-y-3 rounded-lg border border-gray-200 p-3">
                    <div className="text-[10px] tracking-widest text-gray-400 uppercase">
                      From category · {selectedCategory.name}
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                      {selectedCategory.attributes.map((catAttr) => {
                        const current = productForm.attributes.find(
                          (a) => a.name === catAttr.attribute,
                        )
                        const currentValue = current?.values?.[0] ?? ""
                        return (
                          <label className="space-y-1" key={catAttr.id}>
                            <div className="flex items-center gap-1.5">
                              <span className="text-[10px] tracking-widest text-gray-500 uppercase">
                                {catAttr.label || catAttr.attribute}
                              </span>
                              {catAttr.unit && (
                                <span className="text-[10px] text-gray-400">({catAttr.unit})</span>
                              )}
                            </div>
                            <input
                              className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                              type={catAttr.dataType === "NUMBER" ? "number" : "text"}
                              value={currentValue.toString()}
                              placeholder={catAttr.dataType === "NUMBER" ? "0" : "—"}
                              onChange={(e) => handleCategoryAttributeChange(e, catAttr)}
                            />
                          </label>
                        )
                      })}
                    </div>
                  </div>
                )}

                {/* Custom / extra attributes */}
                <div className="space-y-2 rounded-lg border border-gray-200 p-3">
                  <div className="text-[10px] tracking-widest text-gray-400 uppercase">
                    Custom attributes
                  </div>

                  {customAttributes.length === 0 && (
                    <div className="text-[11px] text-gray-400">No custom attributes yet.</div>
                  )}

                  {customAttributes.map((attr, idx) => (
                    <div key={idx} className="flex items-center gap-2">
                      <input
                        className="w-2/5 rounded-lg border border-gray-300 bg-white px-3 py-2 font-mono text-xs"
                        placeholder="name"
                        value={attr.name}
                        onChange={(e) =>
                          setCustomAttributes((prev) =>
                            prev.map((a, i) =>
                              i === idx
                                ? {
                                    ...a,
                                    name: e.target.value,
                                  }
                                : a,
                            ),
                          )
                        }
                      />
                      <span className="text-gray-300">·</span>
                      <input
                        className="flex-1 rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                        placeholder="value"
                        value={attr.values.join("")}
                        onChange={(e) =>
                          setCustomAttributes((prev) =>
                            prev.map((a, i) =>
                              i === idx
                                ? {
                                    ...a,
                                    values: [e.target.value],
                                  }
                                : a,
                            ),
                          )
                        }
                      />
                      <button
                        type="button"
                        onClick={() =>
                          setCustomAttributes((prev) => prev.filter((_, i) => i !== idx))
                        }
                        className="shrink-0 rounded border border-red-200 px-2 py-1.5 text-[10px] text-red-500 hover:bg-red-50"
                      >
                        ✕
                      </button>
                    </div>
                  ))}

                  <button
                    type="button"
                    onClick={() =>
                      setCustomAttributes((prev) => [
                        ...prev,
                        {
                          name: "",
                          values: [],
                        },
                      ])
                    }
                    className="mt-1 text-[11px] text-[#c8a96e] hover:underline"
                  >
                    + Add attribute
                  </button>
                </div>
              </div>

              {/*Image upload*/}
              <div className="col-span-2 space-y-2">
                <FieldLabel>Images</FieldLabel>

                {/* existing images */}
                {existingImages.length > 0 && (
                  <div className="space-y-2">
                    {existingImages.map((img) => (
                      <div
                        key={img.id}
                        className={`flex gap-3 rounded-lg border p-2 transition-colors ${
                          img.removed
                            ? "border-red-200 bg-red-50 opacity-60"
                            : "border-gray-200 bg-gray-50"
                        }`}
                      >
                        {/* thumbnail */}
                        <div className="relative shrink-0">
                          <img
                            src={img.url}
                            alt={img.altText ?? ""}
                            className="h-16 w-16 rounded object-cover"
                          />
                          {img.removed && (
                            <div className="absolute inset-0 flex items-center justify-center rounded bg-black/30">
                              <span className="text-[10px] font-semibold text-white">REMOVED</span>
                            </div>
                          )}
                        </div>

                        {/* fields */}
                        <div className="flex min-w-0 flex-1 flex-col gap-1.5">
                          <div className="grid grid-cols-3 gap-2">
                            <div className="col-span-2 space-y-0.5">
                              <div className="text-[10px] tracking-widest text-gray-400 uppercase">
                                Alt text
                              </div>
                              <input
                                disabled={img.removed}
                                className="w-full rounded border border-gray-300 bg-white px-2 py-1 text-xs disabled:opacity-50"
                                value={img.altText ?? ""}
                                onChange={(e) =>
                                  updateExisting(img.id, {
                                    altText: e.target.value,
                                  })
                                }
                              />
                            </div>
                            <div className="space-y-0.5">
                              <div className="text-[10px] tracking-widest text-gray-400 uppercase">
                                Order
                              </div>
                              <input
                                disabled={img.removed}
                                type="number"
                                className="w-full rounded border border-gray-300 bg-white px-2 py-1 text-xs disabled:opacity-50"
                                value={img.sortOrder}
                                onChange={(e) =>
                                  updateExisting(img.id, {
                                    sortOrder: e.target.value,
                                  })
                                }
                              />
                            </div>
                          </div>
                          <div className="truncate text-[10px] text-gray-400">{img.fileName}</div>
                        </div>

                        {/* remove / restore */}
                        <div className="flex shrink-0 items-start">
                          {img.removed ? (
                            <button
                              type="button"
                              onClick={() => restoreExisting(img.id)}
                              className="rounded border border-gray-300 px-2 py-1 text-[10px] text-gray-600 hover:bg-white"
                            >
                              Restore
                            </button>
                          ) : (
                            <button
                              type="button"
                              onClick={() => removeExisting(img.id)}
                              className="rounded border border-red-200 px-2 py-1 text-[10px] text-red-500 hover:bg-red-50"
                            >
                              Remove
                            </button>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* new images */}
                {newImages.length > 0 && (
                  <div className="space-y-2">
                    {newImages.map((img, idx) => (
                      <div
                        key={idx}
                        className="flex gap-3 rounded-lg border border-dashed border-[#c8a96e]/60 bg-amber-50/30 p-2"
                      >
                        {/* preview */}
                        <div className="relative shrink-0">
                          <img
                            src={img.previewUrl}
                            alt=""
                            className="h-16 w-16 rounded object-cover"
                          />
                          <span className="absolute -top-1 -left-1 rounded-full bg-[#c8a96e] px-1.5 py-0.5 text-[9px] font-semibold text-white">
                            NEW
                          </span>
                        </div>

                        {/* fields */}
                        <div className="flex min-w-0 flex-1 flex-col gap-1.5">
                          <div className="grid grid-cols-3 gap-2">
                            <div className="col-span-2 space-y-0.5">
                              <div className="text-[10px] tracking-widest text-gray-400 uppercase">
                                Alt text
                              </div>
                              <input
                                className="w-full rounded border border-gray-300 bg-white px-2 py-1 text-xs"
                                value={img.altText ?? ""}
                                onChange={(e) =>
                                  updateNew(idx, {
                                    altText: e.target.value,
                                  })
                                }
                              />
                            </div>
                            <div className="space-y-0.5">
                              <div className="text-[10px] tracking-widest text-gray-400 uppercase">
                                Order
                              </div>
                              <input
                                type="number"
                                className="w-full rounded border border-gray-300 bg-white px-2 py-1 text-xs"
                                value={img.sortOrder}
                                onChange={(e) =>
                                  updateNew(idx, {
                                    sortOrder: e.target.value,
                                  })
                                }
                              />
                            </div>
                          </div>
                          <div className="truncate text-[10px] text-gray-400">{img.file.name}</div>
                        </div>

                        {/* discard */}
                        <div className="flex shrink-0 items-start">
                          <button
                            type="button"
                            onClick={() => removeNew(idx)}
                            className="rounded border border-red-200 px-2 py-1 text-[10px] text-red-500 hover:bg-red-50"
                          >
                            Discard
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* file picker */}
                <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-dashed border-gray-300 px-3 py-2.5 text-xs text-gray-400 hover:border-gray-400 hover:text-gray-600">
                  <span>＋ Add images</span>
                  <input
                    type="file"
                    multiple
                    accept="image/*"
                    className="hidden"
                    onChange={(e) => addNewFiles(e.target.files)}
                    // reset value so picking the same file twice works
                    onClick={(e) => {
                      ;(e.target as HTMLInputElement).value = ""
                    }}
                  />
                </label>
              </div>
            </div>

            <ModalFooter
              mode={productModal.mode}
              onCancel={() =>
                setProductModal({
                  open: false,
                  mode: "create",
                })
              }
              onSubmit={submitProduct}
            />
          </div>
        </Popup>
      )}

      {activeTab === "bulk" && <BulkUpload />}
    </div>
  )
}