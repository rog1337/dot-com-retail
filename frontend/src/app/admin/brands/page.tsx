"use client"
import Card from "@/src/components/admin/Card"
import { useEffect, useState } from "react"
import { useToastStore } from "@store/toastStore"
import { adminApi } from "@lib/api/adminApi"
import { defaultPageMetadata, PageMetadata } from "@_types/page"
import { AdminBrand } from "@_types/admin"
import TopBar from "@components/admin/TopBar"
import Popup from "@components/Popup"
import Loading from "@components/Loading"
import BottomBar from "@components/admin/BottomBar"
import FieldLabel from "@components/admin/FieldLabel"
import ModalHeader from "@components/admin/ModalHeader"
import ModalFooter from "@components/admin/ModalFooter"
import { logger } from "@lib/logger"

interface BrandForm {
  name: string
}

const defaultBrandForm = { name: "" }

export default function AdminBrandsPage() {
  const [brands, setBrands] = useState<AdminBrand[]>([])
  const [pageMetadata, setPageMetadata] = useState<PageMetadata>(defaultPageMetadata)
  const [loading, setLoading] = useState(false)
  const [pageIndex, setPageIndex] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [brandQuery, setBrandQuery] = useState("")
  const { show } = useToastStore()

  const [brandModal, setBrandModal] = useState<{
    open: boolean
    mode: "create" | "edit"
    brand?: AdminBrand
  }>({ open: false, mode: "create" })

  const [brandForm, setBrandForm] = useState<BrandForm>(defaultBrandForm)

  useEffect(() => {
    setLoading(true)
    const timeout = setTimeout(() => {
      fetchBrands()
      setLoading(false)
    }, 300)
    return () => clearTimeout(timeout)
  }, [brandQuery, pageSize, pageIndex])

  const fetchBrands = async (query = brandQuery, page = pageIndex) => {
    if (query.startsWith("#")) {
      const id = query.substring(1)
      if (!id) return
      try {
        const brand = await adminApi.getBrand(id)
        setBrands([brand])
        setPageMetadata((p) => ({ ...p, totalPages: 1 }))
      } catch (e) {
        setBrands([])
        if (e?.status === 404 || e?.status === 400) return
        show("Error fetching brand by id", "error")
        logger.d("Error fetching brand by id", e)
      } finally {
      }
      return
    }
    try {
      const res = await adminApi.getBrandsByText(query, page, pageSize)
      setBrands(res.content)
      setPageMetadata(res.page)
    } catch (e) {
      if (e?.status === 400) return
      show("Error loading brands", "error")
    }
  }

  const openCreateBrand = () => {
    setBrandModal({ open: true, mode: "create" })
    setBrandForm(defaultBrandForm)
  }

  const openEditBrand = (b: AdminBrand) => {
    setBrandModal({ open: true, mode: "edit", brand: b })
    setBrandForm({ name: b.name ?? "" })
  }

  const submitBrand = async () => {
    const name = brandForm.name.trim()
    if (!name) {
      show("Brand name is required", "error")
      return
    }

    const data = { name }

    try {
      if (brandModal.mode === "create") {
        await adminApi.createBrand(data)
        show("Brand created", "success")
      } else {
        if (!brandModal.brand) return
        await adminApi.editBrand({
          id: brandModal.brand.id,
          name,
        })
        show("Brand updated", "success")
      }
      setBrandModal({ open: false, mode: "create" })
      await fetchBrands()
    } catch (e) {
      if (e?.response?.data?.code === "BRAND_NOT_FOUND") {
        show(e?.response?.data?.detail, "error")
        return
      }
      show("Failed saving brand", "error")
    }
  }

  const deleteBrand = async (b: AdminBrand) => {
    if (!confirm(`Delete "${b.name}"? This cannot be undone.`)) return
    try {
      await adminApi.deleteBrand(b.id)
      show("Brand deleted", "success")
      await fetchBrands()
    } catch {
      show("Failed deleting brand", "error")
    }
  }

  return (
    <div className="mx-auto max-w-6xl space-y-5">
      <div>
        {pageMetadata && (
          <TopBar
            page={pageMetadata}
            buttons={[{ name: "+ New Brand", onClick: openCreateBrand }]}
            onSetPageSize={setPageSize}
            onSearch={(q) => {
              setBrandQuery(q)
              setPageIndex(0)
            }}
          />
        )}
        <Card title="Brands">
          {loading ? (
            <div className="py-8">
              <Loading />
            </div>
          ) : brands.length === 0 ? (
            <div className="py-10 text-center text-xs text-gray-400">No brands found.</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-xs">
                <thead>
                  <tr className="text-left tracking-widest text-gray-400 uppercase">
                    <th className="pr-3 pb-2">ID</th>
                    <th className="pr-3 pb-2">Name</th>
                    <th className="pb-2 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {brands.map((c) => (
                    <tr
                      key={c.id}
                      className="border-t border-gray-100 hover:bg-zinc-50 dark:hover:bg-zinc-800"
                    >
                      <td className="py-3 pr-3">
                        <div className="font-medium">{c.id}</div>
                      </td>
                      <td className="py-3 pr-3">{c.name}</td>
                      <td className="py-3 text-right">
                        <div className="flex justify-end gap-2">
                          <button
                            type="button"
                            onClick={() => openEditBrand(c)}
                            className="rounded-lg border border-gray-200 px-3 py-1 hover:bg-gray-50"
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            onClick={() => deleteBrand(c)}
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

        {pageMetadata && pageMetadata.totalPages > 1 && (
          <BottomBar page={pageIndex} setPage={setPageIndex} totalPages={pageMetadata.totalPages} />
        )}
      </div>

      {brandModal.open && (
        <Popup>
          <div className="space-y-4">
            <ModalHeader
              title={brandModal.mode === "edit" ? "Edit brand" : "New brand"}
              onClose={() => setBrandModal({ open: false, mode: "create" })}
            ></ModalHeader>

            <div className="grid grid-cols-2 gap-3">
              <label className="space-y-1">
                <FieldLabel>Name</FieldLabel>
                <input
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  value={brandForm.name}
                  onChange={(e) => setBrandForm((f) => ({ ...f, name: e.target.value }))}
                />
              </label>
            </div>
            <ModalFooter
              mode={brandModal.mode}
              onCancel={() => setBrandModal({ open: false, mode: "create" })}
              onSubmit={submitBrand}
            />
          </div>
        </Popup>
      )}
    </div>
  )
}
