"use client"

import {
  Combobox,
  ComboboxButton,
  ComboboxInput,
  ComboboxOption,
  ComboboxOptions,
} from "@headlessui/react"
import { ChevronDownIcon } from "lucide-react"
import { useState } from "react"

export interface ComboboxComponentProps {
  options: string[]
  selected: string
  setSelected: (value: string | null) => void
  onQuery: (query: string) => void
  placeholder: string
  loading: boolean
}

export default function ComboboxComponent({
  options,
  selected,
  setSelected,
  onQuery,
  placeholder,
  loading,
}: ComboboxComponentProps) {
  const [_selected, _setSelected] = useState<string | null>(selected)

  const handleChange = (value: string | null) => {
    _setSelected(value)
    setSelected(value)
  }

  return (
    <div className="relative">
      <Combobox value={_selected} onChange={handleChange}>
        <div className="relative">
          <ComboboxInput
            className="w-full rounded-lg border px-2 py-2 text-xs focus:ring focus:outline-none"
            displayValue={(value: string) => value ?? ""}
            onChange={(e) => onQuery(e.target.value)}
            placeholder={placeholder}
          />

          <ComboboxButton className="group absolute inset-y-0 right-0 px-2.5">
            <ChevronDownIcon className="size-4 fill-white/60 group-data-hover:fill-white" />
          </ComboboxButton>
        </div>

        {/* Dropdown */}
        <ComboboxOptions className="bg-background absolute z-10 mt-1 max-h-60 w-full rounded-lg border shadow">
          {loading && <div className="px-4 py-2 text-sm text-gray-500">Loading...</div>}

          {/*{!loading && options.length === 0 && query.length > 1 && (*/}
          {!loading && options.length === 0 && (
            <div className="px-4 py-2 text-sm text-gray-500">No results found</div>
          )}

          {options.map((option) => (
            <ComboboxOption
              key={option}
              value={option}
              className="data-focus:bg-hover-background p-1 text-xs data-focus:rounded-md"
            >
              {option}
            </ComboboxOption>
          ))}
        </ComboboxOptions>
      </Combobox>
    </div>
  )
}
