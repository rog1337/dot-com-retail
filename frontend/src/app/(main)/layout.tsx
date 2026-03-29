import Footer from "@components/Footer"
import Header from "@components/Header"
import { ReactNode } from "react";

export default function MainLayout({ children }: { children: ReactNode }) {
  return (
    <>
      <Header />
        <main className="flex-grow">
          {children}
        </main>
      <Footer />
    </>
  );
}
