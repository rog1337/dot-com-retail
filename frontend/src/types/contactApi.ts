import api from "@lib/api/api"

export interface ContactEmail {
  email: string
  name: string
  message: string
}

const paths = {
  base: "/contact",
}

export const contactApi = {
  sendContactEmail: (data: ContactEmail) =>
    api.post(paths.base, data),
}