import {Loader} from "lucide-react";

export default function Loading() {
    return (
        <div className="flex items-center justify-center mt-50">
            <Loader className="h-8 w-8 animate-spin [animation-duration:2s]" />
        </div>
    )
}