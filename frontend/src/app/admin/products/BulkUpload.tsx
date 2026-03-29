"use client"

import Card from "@components/admin/Card"
import { useState } from "react"
import { useToastStore } from "@store/toastStore"
import { adminApi } from "@lib/api/adminApi"
import { CreateProductRequest } from "@_types/admin"

type BulkMode = "auto" | "json" | "csv"

export default function BulkUpload() {
  const { show } = useToastStore()
  const [bulkFile, setBulkFile] = useState<File | null>(null)
  const [bulkMode, setBulkMode] = useState<BulkMode>("auto")
  const [bulkBusy, setBulkBusy] = useState(false)
  const [bulkResult, setBulkResult] = useState<null | {
    saved: number
    failed: { index: number; message?: string }[]
  }>(null)

  function parseBool(v: string): boolean {
    const t = v.trim().toLowerCase()
    return t === "true" || t === "1" || t === "yes" || t === "on"
  }

  function parseCsv(text: string): Array<Record<string, string>> {
    const lines = text
      .split(/\r?\n/)
      .map((l) => l.trim())
      .filter(Boolean)

    if (lines.length === 0) return []

    const header = splitCsvLine(lines[0]).map((h) => h.trim())
    const rows = lines.slice(1)

    return rows.map((line) => {
      const cols = splitCsvLine(line)
      const obj: Record<string, string> = {}
      header.forEach((h, idx) => {
        obj[h] = (cols[idx] ?? "").trim()
      })
      return obj
    })
  }

  function splitCsvLine(line: string): string[] {
    // Simple CSV splitter: handles quoted fields with commas inside.
    const out: string[] = []
    let cur = ""
    let inQuotes = false

    for (let i = 0; i < line.length; i++) {
      const ch = line[i]
      if (ch === '"' && (i === 0 || line[i - 1] !== "\\")) {
        inQuotes = !inQuotes
        continue
      }
      if (ch === "," && !inQuotes) {
        out.push(cur)
        cur = ""
        continue
      }
      cur += ch
    }
    out.push(cur)
    return out
  }

  function JsonOrThrow<T = unknown>(text: string): T {
    return JSON.parse(text) as T
  }

  const bulkUploadProducts = async () => {
    if (!bulkFile) {
      show("Select a JSON or CSV file first", "error")
      return
    }

    setBulkBusy(true)
    setBulkResult(null)

    try {
      const text = await bulkFile.text()
      const inferredMode =
        bulkMode !== "auto"
          ? bulkMode
          : bulkFile.name.toLowerCase().endsWith(".csv")
            ? "csv"
            : "json"

      let rows: Array<Record<string, string>> = []
      let productsToCreate = []

      if (inferredMode === "json") {
        const parsed = JsonOrThrow<any>(text)
        if (!Array.isArray(parsed)) {
          throw new Error("JSON file must contain an array of products")
        }
        productsToCreate = parsed
      } else {
        rows = parseCsv(text)
        productsToCreate = rows
      }

      const failed: { index: number; message?: string }[] = []
      let saved = 0

      // Sequential create for safer testing feedback
      for (let i = 0; i < productsToCreate.length; i++) {
        const raw = productsToCreate[i] ?? {}
        try {
          const name = String(raw.name ?? raw.productName ?? "").trim()
          const sku = String(raw.sku ?? "").trim()

          const price = Number(raw.price ?? 0)
          const salePrice = Number(raw.salePrice ?? raw.sale_price ?? raw.sale_price ?? 0)
          const stock = Number(raw.stock ?? 0)

          const description =
            raw.description === undefined || raw.description === null
              ? null
              : String(raw.description)

          const brandIdRaw = raw.brandId ?? raw.brand_id ?? raw.brand
          const categoryIdRaw = raw.categoryId ?? raw.category_id ?? raw.category

          const brandId =
            brandIdRaw === undefined || brandIdRaw === null || String(brandIdRaw).trim() === ""
              ? null
              : Number(brandIdRaw)

          const categoryId =
            categoryIdRaw === undefined ||
            categoryIdRaw === null ||
            String(categoryIdRaw).trim() === ""
              ? null
              : Number(categoryIdRaw)

          if (!name || !sku) {
            throw new Error("Missing required fields: name and sku")
          }
          if (!Number.isFinite(price) || !Number.isFinite(salePrice) || !Number.isFinite(stock)) {
            throw new Error("price/salePrice/stock must be numbers")
          }

          const payload: CreateProductRequest = {
            name,
            sku,
            description: description ? description : null,
            price: Number(price),
            salePrice: Number(salePrice),
            stock: Number.isFinite(stock) ? Math.trunc(stock) : 0,
            brandId: Number.isFinite(brandId) ? (brandId as number) : null,
            categoryId: Number.isFinite(categoryId) ? (categoryId as number) : null,
            isActive: true,
            images: [],
            attributes: [],
          }

          await adminApi.createProduct(payload, [])
          saved++
        } catch (e) {
          failed.push({
            index: i,
            message: e?.message ?? "Failed to create product",
          })
        }
      }

      setBulkResult({ saved, failed })
      show("Bulk upload finished", "success")
    } catch (e) {
      show(e?.message ?? "Bulk upload failed", "error")
    } finally {
      setBulkBusy(false)
    }
  }

  return (
    <Card title="Bulk Upload (products JSON/CSV)">
      <div className="space-y-3">
        <div className="grid grid-cols-2 gap-3">
          <label className="space-y-1">
            <div className="text-[10px] tracking-widest text-gray-400 uppercase">File</div>
            <input
              type="file"
              accept=".json,.csv,application/json,text/csv"
              onChange={(e) => setBulkFile(e.target.files?.[0] ?? null)}
              className="text-xs"
            />
          </label>

          <label className="space-y-1">
            <div className="text-[10px] tracking-widest text-gray-400 uppercase">Mode</div>
            <select
              className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs"
              value={bulkMode}
              onChange={(e) => setBulkMode(e.target.value as BulkMode)}
            >
              <option value="auto">Auto</option>
              <option value="json">JSON</option>
              <option value="csv">CSV</option>
            </select>
          </label>
        </div>

        <div className="text-xs text-gray-500">
          CSV/JSON should include: `name`, `sku`, `price`, `salePrice`, `stock`, `brandId`,
          `categoryId`, optional `description`.
        </div>

        <div className="flex justify-end">
          <button
            type="button"
            disabled={bulkBusy}
            onClick={bulkUploadProducts}
            className="rounded-lg bg-[#c8a96e] px-4 py-2 text-xs text-black hover:bg-[#d4b87e] disabled:opacity-50"
          >
            {bulkBusy ? "Uploading..." : "Upload"}
          </button>
        </div>

        {bulkResult && (
          <div className="mt-4 space-y-3">
            <div className="rounded-lg border border-gray-200 p-3 text-xs">
              <div className="font-semibold">Saved: {bulkResult.saved}</div>
              <div className="mt-1 text-gray-500">Failed: {bulkResult.failed.length}</div>
            </div>

            {bulkResult.failed.length > 0 && (
              <div className="rounded-lg border border-gray-200 p-3 text-xs">
                <div className="mb-2 tracking-widest text-gray-400 uppercase">Failures</div>
                <div className="space-y-2">
                  {bulkResult.failed.slice(0, 25).map((f) => (
                    <div key={f.index} className="flex gap-3">
                      <div className="font-semibold">#{f.index}</div>
                      <div className="text-gray-600">{f.message ?? "Failed"}</div>
                    </div>
                  ))}
                  {bulkResult.failed.length > 25 && (
                    <div className="text-gray-400">Showing first 25 failures...</div>
                  )}
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </Card>
  )
}
