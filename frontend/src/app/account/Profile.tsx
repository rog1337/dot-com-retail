import Section from "src/app/account/Section"
import Field from "src/app/account/Field"
import {useEffect, useState} from "react"
import { useAuth } from "@/src/lib/auth/authContext"
import {accountApi} from "@lib/api/accountApi"
import {logger} from "@lib/logger"
import {useToastStore} from "@store/toastStore"
import Loading from "@components/Loading";

export default function Profile() {
    const { user, setUser } = useAuth()
    const { show } = useToastStore()
    const [editingName, setEditingName] = useState(false)
    const [nameInput, setNameInput] = useState(user!.displayName)
    const [email, setEmail] = useState<string | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    if (user === null) return

    useEffect(() => {
        const fetchUserDetails = async () => {
            try {
                const userDetails = await accountApi.getAccountDetails()
                setEmail(userDetails.email)
            } catch(e: any) {
                logger.error("Error fetching user details", e)
                show("Error fetching user details", "error")
            }
            setIsLoading(false)
        }

        fetchUserDetails()
    }, [])

    const saveName = async () => {
        try {
            const updateRequest = {
                displayName: nameInput.trim()
            }
            const user = await accountApi.updateAccount(updateRequest)
            setUser(user)
        } catch(e: any) {
            logger.d("Error updating user display name", e)
            show("There was an error updating your name", "error")
        } finally {
            setEditingName(false)
        }
    }

    return (
        <div className="fade-in">
            <Section title="Profile">
                {isLoading &&
                    <Loading/>
                }
                <Field label="Display name">
                    {editingName ? (
                        <div className="flex flex-col gap-2 fade-in">
                            <input
                                type="text"
                                autoFocus
                                value={nameInput}
                                onChange={e => setNameInput(e.target.value)}
                                onKeyDown={e => e.key === "Enter" && saveName()}
                                className="w-full border border-zinc-200 rounded-lg px-3 py-2 outline-none focus:border-zinc-400 transition-colors"
                            />
                            <div className="flex gap-2">
                                <button onClick={saveName}
                                        className="flex-1 text-sm font-medium text-white bg-zinc-800 py-2.5 rounded-lg hover:bg-zinc-700 transition-colors">
                                    Save
                                </button>
                                <button onClick={() => {
                                    setEditingName(false)
                                    setNameInput(user.displayName)
                                }}
                                        className="flex-1 text-sm text-zinc-400 py-2.5 rounded-lg hover:bg-zinc-200 transition-colors">
                                    Cancel
                                </button>
                            </div>
                        </div>
                    ) : (
                        <div className="flex items-center justify-between">
                            <span className="text-sm text-zinc-700 truncate">{user.displayName}</span>
                            <button onClick={() => setEditingName(true)}
                                    className="text-xs text-zinc-400 hover:text-zinc-700 transition-colors ml-3 shrink-0 py-1 px-1">
                                Edit
                            </button>
                        </div>
                    )}
                </Field>
                <Field label="Email">
                    <span className="text-sm text-zinc-400 truncate block">{email}</span>
                </Field>
            </Section>
        </div>
    )
}