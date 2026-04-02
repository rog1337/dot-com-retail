"use client"
import { FilterField, FilterResponse } from "@_types/filter"
import { useState } from "react"
import { logger as log } from "@lib/logger"
import { ChevronDown } from "lucide-react"
import { ProductQuery } from "@_types/product"

type FilterProps = {
  filter: FilterField
  onChange: (param: string, values: (string | number | boolean)[]) => void
}

export default function Filter({ filter, onChange }: FilterProps) {
  const { name, filterType, values, label }: FilterField = filter
  const [selectedValues, setSelectedValues] = useState<(string | number | boolean)[]>(
    values.flatMap((item) => (item.isEnabled ? [item.id ? item.id : item.value] : [])),
  )
  const [open, setOpen] = useState(selectedValues.length > 0)

  const handleChange = (value: string | number | boolean) => {
    const idx = selectedValues.indexOf(value)
    if (idx > -1) {
      const newValues = [...selectedValues]
      newValues.splice(idx, 1)
      setSelectedValues(newValues)
      log.d("Removed filter, new values", newValues)
      onChange(name, newValues)
      return
    }

    const newValues = [...selectedValues, value]
    setSelectedValues(newValues)
    log.d("Added filter, new values", newValues)
    onChange(name, newValues)
  }

  const renderValues = () => {
    switch (filterType) {
      case Type.CHECKBOX:
        return checkbox()
      // case Type.DROPDOWN:
      //     return dropdown()
    }
  }

  return (
    <div className="mb-6">
      <button
        type="button"
        onClick={() => setOpen((o) => !o)}
        aria-expanded={open}
        className="mb-1 flex cursor-pointer items-center justify-between text-left text-xl font-bold select-none"
      >
        <span>{label}</span>
        <span className={`transition-transform duration-200${open ? "rotate-180" : "rotate-0"}`}>
          <ChevronDown />
        </span>
      </button>

      <div
        className={`overflow-hidden transition-all duration-200 ease-in-out ${open ? "max-h-96 opacity-100" : "max-h-0 opacity-0"}`}
      >
        <span className="mb-1 block text-gray-500">{selectedValues.length} selected</span>
        {renderValues()}
      </div>
    </div>
  )

  function checkbox() {
    let i = 0
    return (
      <>
        {values.map((item) => (
          <label className="flex cursor-pointer items-center gap-2 text-xl select-none" key={i++}>
            <input
              type="checkbox"
              className="h-4 w-4 accent-blue-600"
              onChange={() => {
                handleChange(item.id ?? item.value)
                item.isEnabled = !item.isEnabled
              }}
              checked={item.isEnabled}
            />
            {item.value}
          </label>
        ))}
      </>
    )
  }
}

export function normalizeFilters(data: FilterResponse, selectedParams: ProductQuery) {
  const { brands, attributes } = data
  const selectedBrands = selectedParams.brands ?? []
  const selectedAttributes = selectedParams.attributes

  const normalizedBrands: FilterField[] = [
    {
      name: "brands",
      label: "Brand",
      values: brands.map((brand) => {
        const isEnabled = selectedBrands.includes(brand.id)
        return { value: brand.name, count: brand.count, id: brand.id, isEnabled: isEnabled }
      }),
      filterType: Type.CHECKBOX,
    },
  ]

  const normalizedAttrs: FilterField[] = attributes.map((attr) => {
    return {
      name: `attr_${attr.attribute}`,
      label: attr.label,
      filterType: attr.filterType,
      values: attr.values.map((attrValue) => {
        const isEnabled =
          selectedAttributes
            ?.find((a) => a.name === attr.attribute)
            ?.values.includes(attrValue.value.toString()) || false
        return {
          value: attrValue.value,
          count: attrValue.count,
          id: attrValue.id,
          isEnabled: isEnabled,
        }
      }),
    }
  })

  return [...normalizedBrands, ...normalizedAttrs]
}

export enum Type {
  CHECKBOX = "CHECKBOX",
  DROPDOWN = "DROPDOWN",
}