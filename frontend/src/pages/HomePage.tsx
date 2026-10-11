import {
  ArrowRight,
  CarFront,
  ClipboardCheck,
  MessageCircle,
  Pause,
  Play,
  Search,
  Users,
} from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/useAuth';

const shortcuts = [
  { icon: CarFront, label: 'My rides', href: '/rides/my' },
  { icon: ClipboardCheck, label: 'My bookings', href: '/bookings/my' },
  { icon: MessageCircle, label: 'Messages', href: '/chats' },
];

function CampusScene() {
  const [paused, setPaused] = useState(false);

  return (
    <div className={`campus-scene ${paused ? 'campus-scene-paused' : ''}`}>
      <div className="campus-scene-heading">
        <span className="inline-flex items-center gap-2">
          <span className="h-1.5 w-1.5 rounded-full bg-blue-400" />
          Different starting points.
        </span>
        <span className="text-slate-300">One shared journey.</span>
      </div>

      <svg
        viewBox="0 0 520 380"
        className="block w-full"
        role="img"
        aria-label="Students sharing a ride to campus"
      >
        {/* Background streets */}
        <g fill="none" stroke="#182c43" strokeWidth="1" strokeLinecap="round">
          <path d="M20 120H155L235 40M35 300H150L300 150H495" />
          <path d="M95 30V100L320 325H490M30 200H180L330 50H480" />
          <path d="M200 360V280L430 50M375 350V250L490 135" />
        </g>

        {/* Small city blocks */}
        <g fill="#102137" stroke="#20354d">
          <rect x="35" y="55" width="38" height="28" rx="7" />
          <rect x="110" y="40" width="55" height="28" rx="7" />
          <rect x="40" y="235" width="45" height="30" rx="7" />
          <rect x="190" y="300" width="50" height="32" rx="7" />
          <rect x="335" y="285" width="48" height="28" rx="7" />
          <rect x="455" y="215" width="30" height="45" rx="7" />
        </g>

        {/* Passenger connections */}
        <g fill="none" stroke="#60a5fa" strokeOpacity=".35" strokeWidth="2" strokeDasharray="4 7">
          <path d="M100 155Q125 155 148 215" />
          <path d="M235 95Q280 140 278 202" />
        </g>

        {/* Shared route */}
        <path
          d="M90 285C160 285 135 215 220 215S305 245 355 190S385 145 425 145"
          fill="none"
          stroke="#162c46"
          strokeWidth="24"
          strokeLinecap="round"
        />
        <path
          d="M90 285C160 285 135 215 220 215S305 245 355 190S385 145 425 145"
          fill="none"
          stroke="#3b82f6"
          strokeOpacity=".65"
          strokeWidth="2"
          strokeLinecap="round"
        />
        <path
          className="campus-route-flow"
          d="M90 285C160 285 135 215 220 215S305 245 355 190S385 145 425 145"
          fill="none"
          stroke="#93c5fd"
          strokeWidth="3"
          strokeDasharray="2 26"
          strokeLinecap="round"
        />

        {/* Starting point */}
        <circle cx="90" cy="285" r="9" fill="#0d1b2e" stroke="#60a5fa" strokeWidth="3" />
        <circle cx="90" cy="285" r="3" fill="#93c5fd" />

        {/* Passenger nodes */}
        <g transform="translate(100 140)">
          <circle className="campus-ring" r="30" fill="none" stroke="#60a5fa" />
          <circle r="23" fill="#102a48" stroke="#31577c" />
          <circle cy="-5" r="5" fill="#93c5fd" />
          <path
            d="M-10 11C-10 1 10 1 10 11"
            fill="none"
            stroke="#93c5fd"
            strokeWidth="3"
            strokeLinecap="round"
          />
        </g>

        <g transform="translate(235 80)">
          <circle className="campus-ring campus-ring-delayed" r="30" fill="none" stroke="#60a5fa" />
          <circle r="23" fill="#102a48" stroke="#31577c" />
          <circle cy="-5" r="5" fill="#93c5fd" />
          <path
            d="M-10 11C-10 1 10 1 10 11"
            fill="none"
            stroke="#93c5fd"
            strokeWidth="3"
            strokeLinecap="round"
          />
        </g>

        {/* Campus */}
        <g transform="translate(420 125)">
          <circle r="52" fill="#0e2636" stroke="#24505b" />

          <path
            d="M-30-10L0-28L30-10"
            fill="#153a50"
            stroke="#7dd3fc"
            strokeWidth="2"
            strokeLinejoin="round"
          />
          <path
            d="M-25-8V22H25V-8M-32 25H32"
            fill="none"
            stroke="#7dd3fc"
            strokeWidth="2"
            strokeLinecap="round"
          />
          <path d="M-15-3V18M-5-3V18M5-3V18M15-3V18" stroke="#7dd3fc" strokeWidth="3" />

          <text y="77" textAnchor="middle" fill="#94a3b8" fontSize="12" fontFamily="inherit">
            Campus
          </text>
        </g>

        {/* Moving ride marker */}
        <g className="campus-car">
          <circle r="23" fill="#3b82f6" fillOpacity=".12" />
          <rect
            x="-17"
            y="-12"
            width="34"
            height="24"
            rx="9"
            fill="#3b82f6"
            stroke="#93c5fd"
            strokeWidth="1.5"
          />
          <path d="M-8-6H8L11 1H-11Z" fill="#bfdbfe" />
          <path d="M-10 6H-7M7 6H10" stroke="white" strokeWidth="2" strokeLinecap="round" />
        </g>
      </svg>

      <div className="campus-scene-footer">
        <div className="flex items-center gap-2 text-xs text-slate-400">
          <Users size={15} aria-hidden="true" />
          Share the ride to campus
        </div>

        <button
          type="button"
          onClick={() => setPaused((current) => !current)}
          aria-label={paused ? 'Play illustration animation' : 'Pause illustration animation'}
          aria-pressed={paused}
          className="campus-animation-control"
        >
          {paused ? <Play size={14} aria-hidden="true" /> : <Pause size={14} aria-hidden="true" />}
        </button>
      </div>
    </div>
  );
}

export function HomePage() {
  const { user } = useAuth();

  return (
    <main className="campus-home">
      <div className="mx-auto max-w-6xl px-5 py-8 sm:px-6 sm:py-10 lg:py-12">
        <section
          aria-labelledby="home-title"
          className="grid items-center gap-10 lg:grid-cols-2 lg:gap-14"
        >
          <div className="home-enter home-delay-1 lg:self-start lg:pt-4">
            <span className="inline-flex items-center gap-2 rounded-full border border-blue-400/25 bg-blue-400/[0.08] px-4 py-2 text-xs font-medium text-blue-200 sm:text-sm">
              <Users size={16} aria-hidden="true" />
              Student carpooling made simple
            </span>

            <h1
              id="home-title"
              className="mt-6 text-4xl font-bold leading-[1.08] tracking-tight text-white sm:text-5xl lg:text-[60px]"
            >
              Same direction.
              <br />
              <span className="text-blue-400">Better company.</span>
            </h1>

            <p className="mt-5 max-w-md text-base leading-7 text-slate-400 sm:text-lg sm:leading-8">
              Heading home or back to campus? Find students going your way and turn your next trip
              into a shared ride.
            </p>

            <div className="home-enter home-delay-3 mt-8 flex flex-col gap-4 sm:flex-row">
              <Link
                to={user ? '/rides' : '/login'}
                className="inline-flex min-h-14 items-center justify-center gap-3 rounded-xl bg-blue-500 px-7 py-3.5 text-base font-semibold text-white shadow-lg shadow-blue-500/20 transition duration-200 hover:-translate-y-0.5 hover:bg-blue-600 hover:shadow-blue-500/30 active:translate-y-0 motion-reduce:transform-none"
              >
                <Search size={21} strokeWidth={2} aria-hidden="true" />
                Find a ride
              </Link>

              <Link
                to={user ? '/rides/create' : '/login'}
                className="inline-flex min-h-14 items-center justify-center gap-3 rounded-xl border border-blue-500/40 bg-white/5 px-7 py-3.5 text-base font-semibold text-blue-400 transition duration-200 hover:-translate-y-0.5 hover:border-blue-400 hover:bg-blue-500/10 hover:text-blue-300 active:translate-y-0 motion-reduce:transform-none"
              >
                <CarFront size={22} strokeWidth={1.9} aria-hidden="true" />
                Offer a ride
              </Link>
            </div>

            <div className="home-enter home-delay-4 mt-8 flex max-w-md items-center gap-3 border-t border-white/[0.08] pt-5">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-blue-400/15 bg-blue-400/[0.06] text-blue-300">
                <MessageCircle size={19} aria-hidden="true" />
              </div>

              <div>
                <p className="text-sm font-medium text-slate-200">Your ride. Your group.</p>
                <p className="mt-1 text-xs leading-5 text-slate-500">
                  Plan pickup details together in your ride chat.
                </p>
              </div>
            </div>
          </div>

          <div className="home-enter home-delay-2 min-w-0">
            <CampusScene />
          </div>
        </section>

        {user && (
          <section
            aria-label="Your travel shortcuts"
            className="home-enter home-delay-4 mt-10 border-t border-white/[0.07] pt-6 lg:mt-12"
          >
            <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
              <p className="text-sm text-slate-400">
                Ready for your next trip,{' '}
                <span className="font-medium text-slate-200">{user.firstName}</span>?
              </p>

              <Link
                to="/dashboard"
                className="inline-flex items-center gap-1.5 text-xs text-slate-500 transition hover:text-blue-300"
              >
                Dashboard
                <ArrowRight size={13} aria-hidden="true" />
              </Link>
            </div>

            <div className="grid gap-3 sm:grid-cols-3">
              {shortcuts.map(({ icon: Icon, label, href }) => (
                <Link key={href} to={href} className="campus-shortcut group">
                  <Icon size={19} aria-hidden="true" className="text-blue-300" />
                  <span className="flex-1 text-sm font-medium">{label}</span>
                  <ArrowRight
                    size={15}
                    aria-hidden="true"
                    className="text-slate-500 transition-transform group-hover:translate-x-1 motion-reduce:transform-none"
                  />
                </Link>
              ))}
            </div>
          </section>
        )}
      </div>
    </main>
  );
}
