import { Link } from 'react-router-dom';
import { RoadAnimation } from '../components/RoadAnimation';
import { useAuth } from '../context/useAuth';

export function HomePage() {
  const { user } = useAuth();

  return (
    <main className="min-h-screen bg-[#060e1a]">
      <section>
        <div className="mx-auto grid min-h-[calc(100vh-73px)] max-w-6xl items-center gap-12 px-6 py-16 lg:grid-cols-2">
          <div className="animate-fade-up flex flex-col">
            <span className="mb-6 inline-flex w-fit items-center gap-2 rounded-full border border-blue-500/30 bg-blue-500/10 px-4 py-1.5 text-sm font-medium text-blue-300">
              <svg className="h-3.5 w-3.5" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2L9.19 8.63 2 9.24l5.46 4.73L5.82 21 12 17.27 18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2z" />
              </svg>
              Student carpooling made simple
            </span>

            <h1 className="text-5xl font-extrabold leading-[1.1] tracking-tight text-white md:text-6xl lg:text-[64px]">
              Share rides <span className="text-blue-400">travel</span> together
            </h1>

            <p className="mt-6 max-w-lg text-lg leading-relaxed text-slate-400">
              Connect with students, find available rides, and save on travel between cities. Safe,
              simple, and reliable.
            </p>

            <div className="mt-9 flex flex-wrap gap-4">
              <Link
                to={user ? '/rides' : '/login'}
                className="flex items-center gap-2 rounded-xl bg-blue-500 px-7 py-3.5 font-semibold text-white shadow-lg shadow-blue-500/20 transition hover:bg-blue-600"
              >
                <svg
                  className="h-4 w-4"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <circle cx="11" cy="11" r="8" />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
                Find a ride
              </Link>
              <Link
                to={user ? '/rides/create' : '/login'}
                className="flex items-center gap-2 rounded-xl border border-blue-500/40 bg-white/5 px-7 py-3.5 font-semibold text-blue-400 transition hover:border-blue-400 hover:text-blue-300"
              >
                <svg
                  className="h-4 w-4"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.75"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M19 17H5a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h1.5L8 5h8l1.5 2H19a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2z" />
                  <circle cx="7.5" cy="17" r="2" />
                  <circle cx="16.5" cy="17" r="2" />
                </svg>
                Offer a ride
              </Link>
            </div>

            <div className="mt-12 flex flex-wrap gap-6">
              <div className="flex items-center gap-3">
                <div className="flex h-11 w-11 items-center justify-center rounded-full border border-blue-500/40 bg-blue-500/15 shadow-md shadow-blue-500/10">
                  <svg
                    className="h-5 w-5 text-blue-400"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                    <polyline points="9 12 11 14 15 10" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-semibold text-white">Verified students</p>
                  <p className="text-xs text-slate-500">Safe &amp; trusted community</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="flex h-11 w-11 items-center justify-center rounded-full border border-yellow-500/40 bg-yellow-500/15 shadow-md shadow-yellow-500/10">
                  <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 24 24" fill="currentColor">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-semibold text-white">Rated drivers</p>
                  <p className="text-xs text-slate-500">Quality you can trust</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="flex h-11 w-11 items-center justify-center rounded-full border border-blue-400/30 bg-blue-400/10 shadow-md shadow-blue-300/10">
                  <svg
                    className="h-5 w-5 text-blue-300"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
                    <line x1="9" y1="10" x2="15" y2="10" />
                    <line x1="9" y1="13" x2="13" y2="13" />
                  </svg>
                </div>
                <div>
                  <p className="text-sm font-semibold text-white">In-app chat</p>
                  <p className="text-xs text-slate-500">Coordinate easily</p>
                </div>
              </div>
            </div>
          </div>

          <div className="animate-fade-up rounded-3xl border border-white/10 shadow-2xl">
            <RoadAnimation />
          </div>
        </div>
      </section>
    </main>
  );
}
