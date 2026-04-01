export default function AboutPage() {
  return (
    <div className="max-w-4xl mx-auto p-6 space-y-8">
      <h1 className="text-4xl font-bold text-center">About Us</h1>

      <section className="space-y-2">
        <h2 className="text-2xl font-semibold">Company Info</h2>
        <p>
          Tyre-dot-com is a student project e-commerce store focused on providing high-quality tyres online.
          Built as a solo full-stack project using Next.js, TypeScript, and Tailwind CSS.
        </p>
      </section>

      <section className="space-y-2">
        <h2 className="text-2xl font-semibold">Our Mission</h2>
        <p>
          Our mission is to make buying tyres simple and convenient for everyone.
          We aim to provide a seamless online experience while showcasing modern web development skills.
        </p>
      </section>

      <section className="space-y-2">
        <h2 className="text-2xl font-semibold">Team</h2>
        <p>Roman Gadjak – Founder & Developer (Solo Project)</p>
      </section>

      <section className="space-y-2">
        <h2 className="text-2xl font-semibold">Links</h2>
        <div className="flex space-x-4">
          <a
            href="https://gitea.kood.tech/romangadjak/i-love-shopping3"
            target="_blank"
            rel="noopener noreferrer"
            className="text-blue-500 hover:underline"
          >
            Gitea
          </a>
        </div>
      </section>
    </div>
  );
}