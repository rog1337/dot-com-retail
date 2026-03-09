"use client"

import { useState } from "react";
import {useAuth} from "@lib/auth/authContext";
import Profile from "@/src/app/account/Profile";
import Security from "@/src/app/account/Security";
import Section from "src/app/account/Section"
import Field from "src/app/account/Field";
import Orders from "@/src/app/account/Orders";

const TABS = ["Profile", "Security", "Orders"];

export default function Account() {
    const { user } = useAuth()
    const [activeTab, setActiveTab] = useState("Profile")
    const [visited, setVisited] = useState<Set<string>>(new Set(["Profile"]))

    if (!user) return null

    const setTab = (tab: string) => {
        setActiveTab(tab)
        setVisited(prev => new Set(prev).add(tab))
    }

    return (
        <>
            <div className="py-6 sm:py-12 px-4 overflow-y-hidden">
                <div className="max-w-xl mx-auto">

                    <div className="mb-8 flex items-center gap-3">
                        <div className="w-11 h-11 rounded-full bg-zinc-800 flex items-center justify-center shrink-0">
              <span className="text-white text-sm font-medium">
                {user.displayName.split(" ").map(n => n[0]).join("").slice(0, 2)}
              </span>
                        </div>
                        <div className="min-w-0">
                            <h1 style={{fontFamily: "'Instrument Serif', Georgia, serif"}}
                                className="text-xl sm:text-2xl leading-tight truncate">{user.displayName}</h1>
                        </div>
                    </div>

                    <div className="flex gap-1 mb-6 border rounded-xl p-1 shadow-sm">
                        {TABS.map(tab => (
                            <button key={tab} onClick={() => setTab(tab)}
                                    className={`bg-zinc-100 dark:bg-zinc-900 flex-1 text-base py-1.5 md:py-2.5 rounded-lg transition-all duration-150 font-medium
                  ${activeTab === tab ? "shadow-sm" : "bg-zinc-300 dark:bg-zinc-700 hover:text-zinc-600 dark:hover:bg-zinc-900`}"}`}>
                                {tab}
                            </button>
                        ))}
                    </div>

                    {visited.has("Profile") && (
                        <div className={activeTab !== "Profile" ? "hidden" : ""}>
                            <Profile />
                        </div>
                    )}
                    {visited.has("Security") && (
                        <div className={activeTab !== "Security" ? "hidden" : ""}>
                            <Security />
                        </div>
                    )}
                    {visited.has("Orders") && (
                        <div className={activeTab !== "Orders" ? "hidden" : ""}>
                            <Orders />
                        </div>
                    )}
                </div>
            </div>
        </>
    );
}