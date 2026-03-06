export interface Contact {
    name: string
    email: string
    phone: string
    address: AddressFields
}

export interface AddressFields {
    streetLine1: string
    streetLine2: string | null
    city: string
    stateOrProvince: string | null
    postalCode: string
    country: string
}