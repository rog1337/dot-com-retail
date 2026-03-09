"use client"

import Section from "@/src/app/account/Section"
import Field from "@/src/app/account/Field"
import React, {useEffect, useState} from "react"
import {authApi} from "@lib/api/authApi";
import {accountApi} from "@lib/api/accountApi";
import {logger} from "@lib/logger";
import {useToastStore} from "@store/toastStore";
import Loading from "@components/Loading";
import Popup from "@components/Popup";
import {twoFAApi} from "@lib/api/twoFAApi";
import Image from "next/image";

export default function Security() {
    const [isResetPasswordDisabled, setIsResetPasswordDisabled] = useState(false)
    const [isLoading, setIsLoading] = useState(false)

    const [twoFAStatus, setTwoFAStatus] = useState(false)
    const [showTwoFASetup, setShowTwoFASetup] = useState(false)
    const [showTwoFADisable, setShowTwoFADisable] = useState(false)
    const [secret, setSecret] = useState("")
    const [qrCode, setQrCode] = useState("")
    const [code, setCode] = useState("")

    const { show } = useToastStore()

    useEffect(() => {
        const fetch2FAStatus = async () => {
            try {
                const {isEnabled} = await twoFAApi.status()
                setTwoFAStatus(isEnabled)
            } catch(e: any) {
                logger.d("Failed fetching 2FA status")
            }
        }

        fetch2FAStatus()
    }, []);

    const resetPassword = async () => {
        try {
            setIsResetPasswordDisabled(true)
            setIsLoading(true)
            await accountApi.resetPassword()
            show("Password reset link sent to your email", "success", 10000)
        } catch (e: any) {
            logger.d("Error resetting password", e)
            show("Error resetting password", "error", 10000)
        } finally {
            setIsResetPasswordDisabled(false)
            setIsLoading(false)
        }
    }

    const setup2FA = async () => {
        try {
            const { secret, qrCode } = await twoFAApi.setup()
            setSecret(secret)
            setQrCode(qrCode)
        } catch(e: any) {
            logger.d("Failed 2FA setup request", e)
        }
    }

    const finish2FA = async () => {
        try {
            const request = { code: code }
            await twoFAApi.verify(request)
            setShowTwoFASetup(false)
            setTwoFAStatus(true)
            show("2FA enabled successfully")
        } catch(e: any) {
            logger.d("Failed 2FA setup verification", e)
            show("Error enabling 2FA", "error", 5000)
        }
    }

    const disable2FA = async () => {
        try {
            if (!code) {
                show("Invalid code", "error")
                return
            }
            const request = { code: code }
            await twoFAApi.disable(request)
            setTwoFAStatus(false)
            setShowTwoFADisable(false)
            show("2FA disabled successfully")
        } catch(e: any) {
            const code = e?.response?.data?.code
            if (code === "INVALID_TWO_FACTOR_CODE") {
                show("Invalid code", "error", 5000)
                return
            }
            if (code === "TWO_FACTOR_REQUIRED") {
                show("Invalid code", "error", 5000)
                return
            }
            show("An error occurred", "error", 5000)
            logger.d("Failed to disable 2FA", e)
        }
    }


    return (
        <div>
            <div className="fade-in">
                <Section title="Security">
                    <Field label="Password">
                        <div className="flex flex-row max-h-10 items-center justify-end">
                            {isLoading &&
                                <Loading/>
                            }
                            <button
                                className="text-base hover:bg-zinc-200 bg-zinc-300 dark:bg-zinc-800 dark:hover:bg-zinc-700
                                transition-colors border-2 border-zinc-400 rounded ml-3 shrink-0 py-1 px-1
                                disabled:opacity-50 disabled:cursor-not-allowed"
                                onClick={resetPassword}
                                disabled={isResetPasswordDisabled}
                            >Reset password</button>
                        </div>
                    </Field>

                    <Field label="2FA">
                        <div className="flex items-center justify-between">
                            <span className="text-base text-zinc-500">{twoFAStatus ? "Enabled" : "Disabled"}</span>

                            {!twoFAStatus ?
                                <button
                                    onClick={() => setShowTwoFASetup(true)}
                                    className="text-base hover:bg-zinc-200 bg-zinc-300 dark:bg-zinc-800 dark:hover:bg-zinc-700
                                transition-colors border-2 border-zinc-400 rounded ml-3 shrink-0 py-1 px-4
                                disabled:opacity-50 disabled:cursor-not-allowed"
                                >Setup
                                </button>
                                :
                                <button
                                    onClick={() => setShowTwoFADisable(true)}
                                    className="text-base hover:bg-zinc-200 bg-zinc-300 dark:bg-zinc-800 dark:hover:bg-zinc-700
                                transition-colors border-2 border-zinc-400 rounded ml-3 shrink-0 py-1 px-4
                                disabled:opacity-50 disabled:cursor-not-allowed"
                                >Disable
                                </button>
                            }
                        </div>
                    </Field>
                </Section>
            </div>

            {showTwoFADisable && <Popup
                children={<div>
                    <h3 style={{fontFamily: "'Instrument Serif', Georgia, serif"}}
                        className="text-2xl mb-2">Disable 2FA</h3>

                    <div className="flex flex-col gap-2 text-center">
                        <span>Code</span>
                        <input
                            className="border border-zinc-300 rounded-lg px-4 text-center"
                            onChange={(e: any) => setCode(e.target.value)}
                        />

                        <button onClick={() => disable2FA()}
                                className="bg-blue-400 mt-2 py-3 rounded-xl
                                         hover:bg-blue-500 transition-colors">
                            Disable
                        </button>

                        <button onClick={() => setShowTwoFADisable(false)}
                                className="flex-1 text-zinc-500 bg-zinc-100 py-3 rounded-xl
                                         hover:bg-zinc-200 transition-colors">
                            Cancel
                        </button>
                    </div>
                </div>
                }
            />
            }

            {showTwoFASetup && (
                <Popup
                    children={
                        <>
                            <h3 style={{fontFamily: "'Instrument Serif', Georgia, serif"}}
                                className="text-2xl mb-2">Setup 2FA</h3>

                            <div className="flex flex-col gap-2">
                                {!secret ?
                                    <>
                                        <p className="mb-5 text-base leading-relaxed">
                                            Click the button below to generate a secret key and a QR-code.
                                        </p>
                                        <button onClick={() => setup2FA()}
                                                className="flex-1 font-medium text-white bg-zinc-800 py-3
                                        rounded-xl hover:bg-zinc-700 transition-colors">
                                            Setup
                                        </button>
                                    </>
                                    :
                                    <div
                                        className="flex flex-col text-center">
                                        <span
                                            className="text-base break-all "
                                        >Secret<p className="text-zinc-600">{secret}</p></span>
                                        <div className="justify-items-center">
                                            <img
                                                className="max-lg"
                                                src={qrCode}
                                                alt={"QR-code"}
                                            />
                                        </div>

                                        <span
                                            className="text-base break-all"
                                        >Enter the 2FA code to finish setup</span>
                                        <input
                                            className="border border-zinc-300 rounded-lg px-4 text-center"
                                            onChange={(e: any) => setCode(e.target.value)}
                                        />

                                        <button onClick={() => finish2FA()}
                                                className="bg-blue-400 mt-2 py-3 rounded-xl
                                         hover:bg-blue-500 transition-colors">
                                            Finish
                                        </button>
                                    </div>
                                }

                                <button onClick={() => setShowTwoFASetup(false)}
                                        className="flex-1 text-zinc-500 bg-zinc-100 py-3 rounded-xl
                                         hover:bg-zinc-200 transition-colors">
                                    Cancel
                                </button>
                            </div>
                        </>
                    }>
                </Popup>
            )}
        </div>
)
}