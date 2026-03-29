import React from "react";

export default function Section({ title, children }: { title: string; children: React.ReactNode }) {
    return (
        <div className="mb-2">
            <h2 style={{ fontFamily: "'Instrument Serif', Georgia, serif", letterSpacing: "-0.01em" }}
                className="text-lg font-normal mb-4">{title}</h2>
            <div className="rounded-2xl border border-zinc-100 shadow-sm overflow-hidden">
                {children}
            </div>
        </div>
    );
}
