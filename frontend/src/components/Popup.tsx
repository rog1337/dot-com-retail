export default function Popup({children}: {children: React.ReactNode}) {
    return (
        <div
            className="overflow-y-auto fixed inset-0 bg-black/20 backdrop-blur-sm flex items-end sm:items-center justify-center z-50 p-4 fade-in">
            <div className="bg-background my-auto rounded-2xl shadow-xl p-6 w-full sm:max-w-xl">
                {children}
            </div>
        </div>

    )
}

