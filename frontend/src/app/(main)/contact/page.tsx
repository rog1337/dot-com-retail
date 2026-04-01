"use client"
import React, { useState } from "react"
import { contactApi } from "@_types/contactApi"

const Contact = () => {
  const [status, setStatus] = useState("")
  const [error, setError] = useState("")

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setStatus("")
    setError("")
    const form = e.target as HTMLFormElement
    const formData = new FormData(form)

    const name = (formData.get("name") as string).trim()
    if (!name) {
      setError("Name is required")
      return
    }

    const email = (formData.get("email") as string).trim()
    if (!email) {
      setError("Email is required")
      return
    }

    const message = (formData.get("message") as string).trim()
    if (!message) {
      setError("Message is required")
      return
    } else if (message.length > 20) {
      setError("Message is too short")
      return
    }

    const data = {
      name: name,
      email: email,
      message: message,
    }

    try {
      await contactApi.sendContactEmail(data)
      setStatus("Message sent successfully!")
      form.reset()
    } catch (e) {
      setError("Error sending message.")
    }
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6 p-6">
      <h1 className="text-center text-4xl font-bold">Contact Us</h1>

      <form className="flex flex-col space-y-4" onSubmit={handleSubmit}>
        <input
          type="text"
          name="name"
          placeholder="Your Name"
          className="rounded border p-2"
          required
        />
        <input
          type="email"
          name="email"
          placeholder="Your Email"
          className="rounded border p-2"
          required
        />
        <textarea
          name="message"
          placeholder="Your Message"
          className="h-32 rounded border p-2"
          required
        />
        <button className="rounded bg-blue-500 px-4 py-2 text-white hover:bg-blue-600">
          Send Message
        </button>
      </form>

      {status && <p className="mt-4 text-center text-green-500">{status}</p>}
      {error && <p className="mt-4 text-center text-red-500">{error}</p>}
    </div>
  )
}

export default Contact