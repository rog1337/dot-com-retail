import React from "react";

export default function Field({ label, children }: { label: string; children: React.ReactNode }) {
    return (
        <div className="flex flex-col sm:flex-row sm:items-center px-5 py-4 border-b border-zinc-50 last:border-0 gap-1.5 sm:gap-0">
            <span className="text-base text-zinc-400 sm:w-36">{label}</span>
            <div className="flex-1 min-w-0">{children}</div>
        </div>
    );
}