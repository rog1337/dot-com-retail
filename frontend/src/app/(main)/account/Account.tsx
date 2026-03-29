"use client"

import { useState } from "react";
import {useAuth} from "@lib/auth/authContext";
import Profile from "@/src/app/(main)/account/Profile";
import Security from "@/src/app/(main)/account/Security";
import Section from "@/src/app/(main)/account/Section"
import Field from "@/src/app/(main)/account/Field";
import Orders from "@/src/app/(main)/account/Orders";

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
        <div className="overflow-y-hidden px-4 py-6 sm:py-12">
          <div className="mx-auto max-w-xl">
            <div className="mb-8 flex items-center gap-3">
              <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-zinc-800">
                <span className="text-sm font-medium text-white">
                  {user.displayName
                    .split(" ")
                    .map((n) => n[0])
                    .join("")
                    .slice(0, 2)}
                </span>
              </div>
              <div className="min-w-0">
                <h1
                  style={{ fontFamily: "'Instrument Serif', Georgia, serif" }}
                  className="truncate text-xl leading-tight sm:text-2xl"
                >
                  {user.displayName}
                </h1>
              </div>
            </div>

            <div className="mb-6 flex gap-1 rounded-xl border p-1 shadow-sm">
              {TABS.map((tab) => (
                <button
                  key={tab}
                  onClick={() => setTab(tab)}
                  className={`flex-1 rounded-lg bg-zinc-100 py-1.5 text-base font-medium transition-all duration-150 md:py-2.5 dark:bg-zinc-900 ${activeTab === tab ? "shadow-sm" : "dark:hover:bg-zinc-900`} bg-zinc-300 hover:text-zinc-600 dark:bg-zinc-700"}`}
                >
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