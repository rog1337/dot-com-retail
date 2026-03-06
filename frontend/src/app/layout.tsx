import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import React from "react";
import {AuthProvider} from "@lib/auth/authContext";
import Header from "@components/Header";
import Footer from "@components/Footer";
import {ThemeProvider} from "next-themes";
import Toast from "@components/Toast";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "dot-com-retail",
  description: "",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body
          className={`${geistSans.variable} ${geistMono.variable} antialiased
          min-h-screen flex flex-col
          `}
      >
      <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
          <AuthProvider>
              <Header/>
              <main className="flex-grow">
                  {children}
                  <Toast/>
              </main>
              <Footer/>
          </AuthProvider>
      </ThemeProvider>
      </body>
    </html>
  );
}
