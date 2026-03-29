"use client"
import Card from "@/src/components/admin/Card"
import { useEffect, useState } from "react"
import { useToastStore } from "@store/toastStore"
import { adminApi } from "@lib/api/adminApi"
import { defaultPageMetadata, PageMetadata } from "@_types/page"
import { AdminCategory, AdminCategoryAttribute } from "@_types/admin"
import TopBar from "@components/admin/TopBar"
import Popup from "@components/Popup"
import Loading from "@components/Loading"
import BottomBar from "@components/admin/BottomBar"
import TabButton from "@/src/components/admin/TabButton"
import Attributes from "@/src/app/admin/categories/Attributes"
import FieldLabel from "@components/admin/FieldLabel"
import ModalHeader from "@components/admin/ModalHeader"
import ModalFooter from "@components/admin/ModalFooter"
import ComboboxComponent from "@components/Combobox"
import { logger } from "@lib/logger"

interface SelectedAttribute {
    id: number
    label: string
}

interface CategoryForm {
    name: string
    selectedAttributes: SelectedAttribute[]
}

const defaultCategoryForm = { name: "", selectedAttributes: [] }

export default function AdminCategoriesPage() {
    const [categories, setCategories] = useState<AdminCategory[]>([])
    const [pageMetadata, setPageMetadata] = useState<PageMetadata>(defaultPageMetadata)
    const [loading, setLoading] = useState(false)
    const [pageIndex, setPageIndex] = useState(0)
    const [pageSize, setPageSize] = useState(10)
    const [categoryQuery, setCategoryQuery] = useState("")
    const [activeTab, setActiveTab] = useState<"categories" | "attributes">("categories")
    const [attributeQuery, setAttributeQuery] = useState("")
    const [attributes, setAttributes] = useState<AdminCategoryAttribute[]>([])
    const [attributesLoading, setAttributesLoading] = useState(false)
    const { show } = useToastStore()

    const [categoryModal, setCategoryModal] = useState<{
        open: boolean
        mode: "create" | "edit"
        category?: AdminCategory
    }>({ open: false, mode: "create" })

    const [categoryForm, setCategoryForm] =
        useState<CategoryForm>(defaultCategoryForm)

    useEffect(() => {
        setLoading(true)
        const timeout = setTimeout(() => {
            fetchCategories()
            setLoading(false)
        }, 300)
        return () => clearTimeout(timeout)
    }, [categoryQuery, pageSize, pageIndex])

    useEffect(() => {
        if (!categoryModal.open) return
        const t = setTimeout(async () => {
            setAttributesLoading(true)
            await fetchAttributes()
            setAttributesLoading(false)
        }, 200)
        return () => clearTimeout(t)
    }, [attributeQuery, categoryModal.open])

    const fetchAttributes = async () => {
        if (attributeQuery.startsWith("#")) {
            const id = attributeQuery.substring(1)
            if (!id) return
            try {
                const attribute = await adminApi.getCategoryAttributeById(id)
                setAttributes([attribute])
            } catch (e) {
                if (
                    e?.response?.data?.code === "CATEGORY_ATTRIBUTE_NOT_FOUND"
                ) {
                    show(e?.response?.data?.detail, "error")
                    return
                }
                show("Error fetching attribute by id", "error")
                logger.d("Error fetching product by id", e)
            }
            return
        }
        try {
            const res = await adminApi.getCategoryAttributesByText(
                attributeQuery,
                0,
                10,
            )
            setAttributes(res.content)
        } catch {
            show("Error loading attributes", "error")
        } finally {
        }
    }

    const fetchCategories = async (query = categoryQuery, page = pageIndex) => {
        if (query.startsWith("#")) {
            const id = query.substring(1)
            if (!id) return
            try {
                const category = await adminApi.getCategory(id)
                setCategories([category])
                setPageMetadata((p) => ({ ...p, totalPages: 1 }))
            } catch (e) {
                setCategories([])
                if (e?.status === 404) return
                show("Error fetching category by id", "error")
                logger.d("Error fetching product by id", e)
            } finally {
            }
            return
        }
        try {
            const res = await adminApi.getCategoriesByText(
                query,
                page,
                pageSize,
            )
            setCategories(res.content)
            setPageMetadata(res.page)
        } catch (e: unknown) {
            show("Error loading categories", "error")
        }
    }

    const openCreateCategory = () => {
        setCategoryModal({ open: true, mode: "create" })
        setCategoryForm(defaultCategoryForm)
    }

    const openEditCategory = (c: AdminCategory) => {
        setCategoryModal({ open: true, mode: "edit", category: c })
        setCategoryForm({
            name: c.name ?? "",
            selectedAttributes:
                c.attributes?.map((a) => ({
                    id: a.id,
                    label: a.label ?? a.attribute,
                })) ?? [],
        })
    }

    const submitCategory = async () => {
        const name = categoryForm.name.trim()
        if (!name) {
            show("Category name is required", "error")
            return
        }

        const attributeIds = categoryForm.selectedAttributes.map((a) => a.id)

        try {
            if (categoryModal.mode === "create") {
                await adminApi.createCategory({
                    name,
                    attributeIds: attributeIds.length ? attributeIds : null,
                })
                show("Category created", "success")
            } else {
                if (!categoryModal.category) return
                await adminApi.editCategory({
                    id: categoryModal.category.id,
                    name,
                    attributeIds: attributeIds.length ? attributeIds : null,
                })
                show("Category updated", "success")
            }
            setCategoryModal({ open: false, mode: "create" })
            await fetchCategories()
        } catch (e) {
            if (e?.response?.data?.code === "CATEGORY_ATTRIBUTE_NOT_FOUND") {
                show(e?.response?.data?.detail, "error")
                return
            }
            show("Failed saving category", "error")
        }
    }

    const deleteCategory = async (c: AdminCategory) => {
        if (!confirm(`Delete "${c.name}"? This cannot be undone.`)) return
        try {
            await adminApi.deleteCategory(c.id)
            show("Category deleted", "success")
            await fetchCategories()
        } catch {
            show("Failed deleting category", "error")
        }
    }

    return (
        <div className="mx-auto max-w-6xl space-y-5">
            <div className="flex flex-wrap gap-2">
                <TabButton
                    active={activeTab === "categories"}
                    onClick={() => setActiveTab("categories")}
                >
                    Categories
                </TabButton>
                <TabButton
                    active={activeTab === "attributes"}
                    onClick={() => setActiveTab("attributes")}
                >
                    Attributes
                </TabButton>
            </div>

            {activeTab === "categories" && (
                <div>
                    {pageMetadata && (
                        <TopBar
                            page={pageMetadata}
                            buttons={[
                                {
                                    name: "+ New Category",
                                    onClick: openCreateCategory,
                                },
                            ]}
                            onSetPageSize={setPageSize}
                            onSearch={(q) => {
                                setCategoryQuery(q)
                                setPageIndex(0)
                            }}
                        />
                    )}
                    <Card title="Categories">
                        {loading ? (
                            <div className="py-8">
                                <Loading />
                            </div>
                        ) : categories.length === 0 ? (
                            <div className="py-10 text-center text-xs text-gray-400">
                                No categories found.
                            </div>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="w-full text-xs">
                                    <thead>
                                        <tr className="text-left tracking-widest text-gray-400 uppercase">
                                            <th className="pr-3 pb-2">ID</th>
                                            <th className="pr-3 pb-2">Name</th>
                                            <th className="pr-3 pb-2">
                                                Attributes
                                            </th>
                                            <th className="pb-2 text-right">
                                                Actions
                                            </th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {categories.map((c) => (
                                            <tr
                                                key={c.id}
                                                className="border-t border-gray-100 hover:bg-zinc-50 dark:hover:bg-zinc-800"
                                            >
                                                <td className="py-3 pr-3">
                                                    <div className="font-medium">
                                                        {c.id}
                                                    </div>
                                                </td>
                                                <td className="py-3 pr-3">
                                                    {c.name}
                                                </td>
                                                <td className="py-3 pr-3">
                                                    {c.attributes?.length}
                                                </td>
                                                <td className="py-3 text-right">
                                                    <div className="flex justify-end gap-2">
                                                        <button
                                                            type="button"
                                                            onClick={() =>
                                                                openEditCategory(
                                                                    c,
                                                                )
                                                            }
                                                            className="rounded-lg border border-gray-200 px-3 py-1 hover:bg-gray-50"
                                                        >
                                                            Edit
                                                        </button>
                                                        <button
                                                            type="button"
                                                            onClick={() =>
                                                                deleteCategory(
                                                                    c,
                                                                )
                                                            }
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
                        <BottomBar
                            page={pageIndex}
                            setPage={setPageIndex}
                            totalPages={pageMetadata.totalPages}
                        />
                    )}
                </div>
            )}

            {activeTab === "attributes" && <Attributes />}

            {categoryModal.open && (
                <Popup>
                    <div className="space-y-4">
                        <ModalHeader
                            title={
                                categoryModal.mode === "edit"
                                    ? "Edit category"
                                    : "New category"
                            }
                            onClose={() =>
                                setCategoryModal({
                                    open: false,
                                    mode: "create",
                                })
                            }
                        ></ModalHeader>

                        <div className="grid grid-cols-2 gap-3">
                            <label className="space-y-1">
                                <FieldLabel>Name</FieldLabel>
                                <input
                                    className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                                    value={categoryForm.name}
                                    onChange={(e) =>
                                        setCategoryForm((f) => ({
                                            ...f,
                                            name: e.target.value,
                                        }))
                                    }
                                />
                            </label>

                            <label className="col-span-2 space-y-1">
                                <FieldLabel>Attribute name or #id</FieldLabel>

                                <ComboboxComponent
                                    options={attributes.map((a) => a.label)}
                                    selected=""
                                    setSelected={(label) => {
                                        if (
                                            categoryForm.selectedAttributes.find(
                                                (a) => a.label === label,
                                            )
                                        ) {
                                            setAttributeQuery("")
                                            return
                                        }
                                        const match = attributes.find(
                                            (r) => r.label === label,
                                        )
                                        if (!match) return
                                        setCategoryForm((f) => ({
                                            ...f,
                                            selectedAttributes: [
                                                ...f.selectedAttributes,
                                                {
                                                    id: match.id,
                                                    label: match.label,
                                                },
                                            ],
                                        }))
                                    }}
                                    onQuery={(q) => setAttributeQuery(q)}
                                    placeholder="Search attributes by name or #id"
                                    loading={attributesLoading}
                                />

                                {categoryForm.selectedAttributes.length > 0 && (
                                    <div className="flex flex-wrap gap-1.5 pt-2">
                                        {categoryForm.selectedAttributes.map(
                                            (a) => (
                                                <span
                                                    key={a.id}
                                                    className="flex items-center gap-1 rounded-full border border-gray-200 bg-gray-50 px-2.5 py-1 text-[11px] text-gray-700"
                                                >
                                                    {a.label}
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            setCategoryForm(
                                                                (f) => ({
                                                                    ...f,
                                                                    selectedAttributes:
                                                                        f.selectedAttributes.filter(
                                                                            (
                                                                                x,
                                                                            ) =>
                                                                                x.id !==
                                                                                a.id,
                                                                        ),
                                                                }),
                                                            )
                                                        }
                                                        className="ml-0.5 leading-none text-gray-400 hover:text-red-500"
                                                    >
                                                        ✕
                                                    </button>
                                                </span>
                                            ),
                                        )}
                                    </div>
                                )}
                            </label>
                        </div>
                        <ModalFooter
                            mode={categoryModal.mode}
                            onCancel={() =>
                                setCategoryModal({
                                    open: false,
                                    mode: "create",
                                })
                            }
                            onSubmit={submitCategory}
                        />
                    </div>
                </Popup>
            )}
        </div>
    )
}
