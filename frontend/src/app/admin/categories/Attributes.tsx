"use client"

import BottomBar from "@components/admin/BottomBar"
import TopBar from "@components/admin/TopBar"
import Card from "@components/admin/Card"
import { AdminCategoryAttribute, AttributeDataType, FilterType } from "@_types/admin"
import { adminApi } from "@lib/api/adminApi"
import { useToastStore } from "@store/toastStore"
import { useEffect, useState } from "react"
import { defaultPageMetadata, PageMetadata } from "@_types/page"
import Loading from "@components/Loading"
import Popup from "@/src/components/Popup"
import ModalFooter from "@components/admin/ModalFooter"
import FieldLabel from "@components/admin/FieldLabel"
import ModalHeader from "@components/admin/ModalHeader"
import { logger } from "@lib/logger"
import { parseIntOrNull } from "@lib/util"

interface AttributeForm {
  attribute: string
  label: string
  unit: string
  dataType: AttributeDataType
  filterType: FilterType
  displayOrder: string
}

const defaultAttributeForm: AttributeForm = {
  attribute: "",
  label: "",
  unit: "",
  dataType: "TEXT",
  filterType: "CHECKBOX",
  displayOrder: "0",
}

const DATA_TYPES: AttributeDataType[] = ["TEXT", "NUMBER", "BOOLEAN"]
const FILTER_TYPES: FilterType[] = ["CHECKBOX", "SLIDER", "DROPDOWN"]

export default function Attributes() {
  const [attributes, setAttributes] = useState<AdminCategoryAttribute[]>([])
  const [attrPageMeta, setAttrPageMeta] = useState<PageMetadata>(defaultPageMetadata)
  const [attrLoading, setAttrLoading] = useState(false)
  const [attrPageIndex, setAttrPageIndex] = useState(0)
  const [attrPageSize, setAttrPageSize] = useState(10)
  const [attributeQuery, setAttributeQuery] = useState("")
  const { show } = useToastStore()

  const [attributeModal, setAttributeModal] = useState<{
    open: boolean
    mode: "create" | "edit"
    attribute?: AdminCategoryAttribute
  }>({ open: false, mode: "create" })

  const [attributeForm, setAttributeForm] = useState<AttributeForm>(defaultAttributeForm)

  useEffect(() => {
    const t = setTimeout(async () => await fetchAttributes(), 300)
    return () => clearTimeout(t)
  }, [attributeQuery, attrPageSize, attrPageIndex])

  const fetchAttributes = async () => {
    if (attributeQuery.startsWith("#")) {
      const id = attributeQuery.substring(1)
      if (!id) return
      try {
        const attribute = await adminApi.getCategoryAttributeById(id)
        setAttributes([attribute])
        setAttrPageMeta((p) => ({ ...p, totalPages: 1 }))
      } catch (e) {
        setAttributes([])
        if (e?.response?.data?.code === "CATEGORY_ATTRIBUTE_NOT_FOUND") {
          return
        }
        show("Error fetching product by id", "error")
        logger.d("Error fetching product by id", e)
      }
      return
    }
    try {
      const res = await adminApi.getCategoryAttributesByText(
        attributeQuery,
        attrPageIndex,
        attrPageSize,
      )
      setAttributes(res.content)
      setAttrPageMeta(res.page)
    } catch {
      show("Error loading attributes", "error")
    } finally {
    }
  }

  const openCreateAttribute = () => {
    setAttributeModal({ open: true, mode: "create" })
    setAttributeForm(defaultAttributeForm)
  }

  const openEditAttribute = (a: AdminCategoryAttribute) => {
    setAttributeModal({ open: true, mode: "edit", attribute: a })
    setAttributeForm({
      attribute: a.attribute ?? "",
      label: a.label ?? "",
      unit: a.unit ?? "",
      dataType: a.dataType ?? "TEXT",
      filterType: a.filterType ?? "CHECKBOX",
      displayOrder: String(a.displayOrder ?? 0),
    })
  }

  const submitAttribute = async () => {
    const attribute = attributeForm.attribute.trim()
    if (!attribute) {
      show("Attribute key is required", "error")
      return
    }
    const label = attributeForm.label.trim()
    if (!label) {
      show("Attribute label is required", "error")
      return
    }

    const displayOrder = parseIntOrNull(attributeForm.displayOrder) ?? 0

    const payload = {
      attribute,
      label,
      unit: attributeForm.unit.trim() || null,
      dataType: attributeForm.dataType,
      filterType: attributeForm.filterType,
      displayOrder,
    }

    try {
      if (attributeModal.mode === "create") {
        await adminApi.createCategoryAttribute(payload)
        show("Attribute created", "success")
      } else {
        if (!attributeModal.attribute) return
        await adminApi.editCategoryAttribute({
          id: attributeModal.attribute.id.toString(),
          ...payload,
        })
        show("Attribute updated", "success")
      }
      setAttributeModal({ open: false, mode: "create" })
      await fetchAttributes()
    } catch {
      show("Failed saving attribute", "error")
    }
  }

  const deleteAttribute = async (a: AdminCategoryAttribute) => {
    if (!confirm(`Delete attribute "${a.label}"? This cannot be undone.`)) return
    try {
      await adminApi.deleteCategoryAttribute(a.id)
      show("Attribute deleted", "success")
      await fetchAttributes()
    } catch {
      show("Failed deleting attribute", "error")
    }
  }

  return (
    <div>
      {attrPageMeta && (
        <TopBar
          page={attrPageMeta}
          buttons={[
            {
              name: "+ New Attribute",
              onClick: openCreateAttribute,
            },
          ]}
          onSetPageSize={setAttrPageSize}
          onSearch={(q) => {
            setAttributeQuery(q)
            setAttrPageIndex(0)
          }}
        />
      )}

      <Card title="Category Attributes">
        {attrLoading ? (
          <div className="py-8">
            <Loading />
          </div>
        ) : attributes.length === 0 ? (
          <div className="py-10 text-center text-xs text-gray-400">No attributes found.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead>
                <tr className="text-left tracking-widest text-gray-400 uppercase">
                  <th className="pr-3 pb-2">ID</th>
                  <th className="pr-3 pb-2">Key</th>
                  <th className="pr-3 pb-2">Label</th>
                  <th className="pr-3 pb-2">Type</th>
                  <th className="pr-3 pb-2">Filter</th>
                  <th className="pr-3 pb-2">Unit</th>
                  <th className="pr-3 pb-2">Order</th>
                  <th className="pr-3 pb-2">Categories</th>
                  <th className="pb-2 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {attributes.map((a) => (
                  <tr
                    key={a.id}
                    className="border-t border-gray-100 hover:bg-zinc-50 dark:hover:bg-zinc-800"
                  >
                    <td className="py-3 pr-3 font-medium">{a.id}</td>
                    <td className="py-3 pr-3 font-mono text-[10px] text-gray-500">{a.attribute}</td>
                    <td className="py-3 pr-3">{a.label}</td>
                    <td className="py-3 pr-3">
                      <DataTypeBadge type={a.dataType} />
                    </td>
                    <td className="py-3 pr-3 text-gray-500">{a.filterType}</td>
                    <td className="py-3 pr-3">{a.unit ?? "—"}</td>
                    <td className="py-3 pr-3">{a.displayOrder}</td>
                    <td className="py-3 pr-3">
                      {a.categories?.length ? (
                        <span className="text-gray-500">
                          {a.categories
                            .slice(0, 3)
                            .map((id) => `#${id}`)
                            .join(", ")}
                          {a.categories.length > 3 ? ` +${a.categories.length - 3}` : ""}
                        </span>
                      ) : (
                        "—"
                      )}
                    </td>
                    <td className="py-3 text-right">
                      <div className="flex justify-end gap-2">
                        <button
                          type="button"
                          onClick={() => openEditAttribute(a)}
                          className="rounded-lg border border-gray-200 px-3 py-1 hover:bg-gray-50"
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          onClick={() => deleteAttribute(a)}
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

      {attrPageMeta && attrPageMeta.totalPages > 1 && (
        <BottomBar
          page={attrPageIndex}
          setPage={setAttrPageIndex}
          totalPages={attrPageMeta.totalPages}
        />
      )}

      {attributeModal.open && (
        <Popup>
          <div className="space-y-4">
            <ModalHeader
              title={attributeModal.mode === "edit" ? "Edit attribute" : "New attribute"}
              onClose={() =>
                setAttributeModal({
                  open: false,
                  mode: "create",
                })
              }
            />

            <div className="grid grid-cols-2 gap-3">
              <label className="space-y-1">
                <FieldLabel>Key</FieldLabel>
                <input
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 font-mono text-xs"
                  placeholder="e.g. screen_size"
                  value={attributeForm.attribute}
                  onChange={(e) =>
                    setAttributeForm((f) => ({
                      ...f,
                      attribute: e.target.value,
                    }))
                  }
                />
              </label>

              <label className="space-y-1">
                <FieldLabel>Label</FieldLabel>
                <input
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  placeholder="e.g. Screen Size"
                  value={attributeForm.label}
                  onChange={(e) =>
                    setAttributeForm((f) => ({
                      ...f,
                      label: e.target.value,
                    }))
                  }
                />
              </label>

              <label className="space-y-1">
                <FieldLabel>Data type</FieldLabel>
                <select
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  value={attributeForm.dataType}
                  onChange={(e) =>
                    setAttributeForm((f) => ({
                      ...f,
                      dataType: e.target.value as AttributeDataType,
                    }))
                  }
                >
                  {DATA_TYPES.map((t) => (
                    <option key={t} value={t}>
                      {t}
                    </option>
                  ))}
                </select>
              </label>

              <label className="space-y-1">
                <FieldLabel>Filter type</FieldLabel>
                <select
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  value={attributeForm.filterType}
                  onChange={(e) =>
                    setAttributeForm((f) => ({
                      ...f,
                      filterType: e.target.value as FilterType,
                    }))
                  }
                >
                  {FILTER_TYPES.map((t) => (
                    <option key={t} value={t}>
                      {t}
                    </option>
                  ))}
                </select>
              </label>

              <label className="space-y-1">
                <FieldLabel>Unit (optional)</FieldLabel>
                <input
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  placeholder='e.g. "inch", "kg" (optional)'
                  value={attributeForm.unit}
                  onChange={(e) =>
                    setAttributeForm((f) => ({
                      ...f,
                      unit: e.target.value,
                    }))
                  }
                />
              </label>

              <label className="space-y-1">
                <FieldLabel>Display order</FieldLabel>
                <input
                  type="number"
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-xs"
                  value={attributeForm.displayOrder}
                  onChange={(e) =>
                    setAttributeForm((f) => ({
                      ...f,
                      displayOrder: e.target.value,
                    }))
                  }
                />
              </label>
            </div>

            <ModalFooter
              mode={attributeModal.mode}
              onCancel={() =>
                setAttributeModal({
                  open: false,
                  mode: "create",
                })
              }
              onSubmit={submitAttribute}
            />
          </div>
        </Popup>
      )}
    </div>
  )
}

function DataTypeBadge({ type }: { type: string }) {
  const colors: Record<string, string> = {
    TEXT: "bg-blue-50 text-blue-600",
    NUMBER: "bg-purple-50 text-purple-600",
    BOOLEAN: "bg-amber-50 text-amber-600",
  }
  return (
    <span
      className={`rounded px-1.5 py-0.5 text-[10px] font-medium ${colors[type] ?? "bg-gray-100 text-gray-500"}`}
    >
      {type}
    </span>
  )
}
