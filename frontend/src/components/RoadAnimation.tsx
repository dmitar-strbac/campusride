export function RoadAnimation() {
  return (
    <div className="relative flex h-full min-h-[440px] w-full items-center justify-center overflow-hidden rounded-3xl bg-[#0d1b2e]">
      <svg className="absolute inset-0 h-full w-full opacity-20" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <pattern id="dots" x="0" y="0" width="28" height="28" patternUnits="userSpaceOnUse">
            <circle cx="1.5" cy="1.5" r="1.5" fill="#334155" />
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="url(#dots)" />
      </svg>

      <div className="pointer-events-none absolute inset-0">
        <div className="absolute left-[22%] top-[35%] h-2 w-2 rounded-full bg-blue-400/70" />
        <div className="absolute left-[78%] top-[30%] h-1.5 w-1.5 rounded-full bg-emerald-400/70" />
        <div className="absolute left-[60%] top-[80%] h-1.5 w-1.5 rounded-full bg-blue-300/50" />
        <div className="absolute left-[40%] top-[72%] h-1 w-1 rounded-full bg-yellow-400/60" />
        <div className="absolute left-[10%] top-[15%] h-1 w-1 rounded-full bg-red-400/60" />
        <div className="absolute left-[60%] top-[40%] h-1 w-1 rounded-full bg-white/60" />
      </div>

      <div className="absolute left-[35%] top-[10%] flex h-16 w-16 items-center justify-center rounded-full border border-blue-500/40 bg-[#0f2544] shadow-lg shadow-blue-500/10">
        <svg
          className="h-7 w-7 text-blue-400"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" />
          <circle cx="12" cy="9" r="2.5" />
        </svg>
      </div>

      <div className="absolute right-[10%] top-[8%] flex h-18 w-18 items-center justify-center rounded-full border border-emerald-500/40 bg-[#0f3028] p-4 shadow-lg shadow-emerald-500/10">
        <svg
          className="h-8 w-8 text-emerald-400"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.75"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
          <circle cx="9" cy="7" r="4" />
          <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
          <path d="M16 3.13a4 4 0 0 1 0 7.75" />
        </svg>
      </div>

      <div className="absolute bottom-[15%] left-[15%] flex h-16 w-16 items-center justify-center rounded-full border border-yellow-500/40 bg-[#1a1a0f] shadow-lg shadow-yellow-500/10">
        <svg className="h-7 w-7 text-yellow-400" viewBox="0 0 24 24" fill="currentColor">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
        </svg>
      </div>

      <div className="absolute bottom-[14%] right-[18%] flex h-16 w-16 items-center justify-center rounded-full border border-blue-400/30 bg-[#0f1e38] shadow-lg shadow-blue-400/10">
        <svg
          className="h-7 w-7 text-blue-300"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.75"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
        </svg>
      </div>

      <svg
        className="absolute inset-0 h-full w-full"
        viewBox="0 0 500 380"
        preserveAspectRatio="xMidYMid meet"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          id="roadPath"
          d="M 60 230 C 120 230, 160 150, 250 190 S 380 270, 440 200"
          fill="none"
          stroke="#334155"
          strokeWidth="2.5"
          strokeDasharray="10 8"
        />

        <circle cx="60" cy="230" r="9" fill="#3B82F6" />
        <circle cx="60" cy="230" r="18" fill="#3B82F6" fillOpacity="0.15" />

        <circle cx="440" cy="200" r="9" fill="#10B981" />
        <circle cx="440" cy="200" r="18" fill="#10B981" fillOpacity="0.15" />

        <g id="movingCar">
          <animateMotion dur="5s" repeatCount="indefinite" rotate="auto">
            <mpath href="#roadPath" />
          </animateMotion>

          <g transform="translate(-20, -11)">
            <rect x="2" y="9" width="36" height="12" rx="4" fill="#3B82F6" />
            <rect x="9" y="2" width="20" height="10" rx="4" fill="#60A5FA" />
            <rect x="10" y="3.5" width="8" height="7" rx="2" fill="#BFDBFE" opacity="0.9" />
            <rect x="20" y="3.5" width="8" height="7" rx="2" fill="#BFDBFE" opacity="0.9" />
            <circle cx="11" cy="22" r="4" fill="#0f172a" />
            <circle cx="11" cy="22" r="2.2" fill="#475569" />
            <circle cx="29" cy="22" r="4" fill="#0f172a" />
            <circle cx="29" cy="22" r="2.2" fill="#475569" />
            <rect x="0" y="12" width="4" height="3" rx="1.5" fill="#EF4444" opacity="0.85" />
            <rect x="38" y="12" width="4" height="3" rx="1.5" fill="#FDE68A" />
          </g>
        </g>
      </svg>
    </div>
  );
}
