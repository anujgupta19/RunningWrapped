# Mobile: React Native + Expo + TypeScript

The developer already knows React and TypeScript well. Explain React Native / Expo-specific details
(native modules, Reanimated worklets, Skia, EAS, platform differences), not React basics.

## Commands

```bash
npx expo start                 # dev server (press i for iOS simulator)
npx expo install <pkg>         # ALWAYS use this for packages with native code. It picks SDK-compatible versions
npm run typecheck              # tsc --noEmit
npm run lint                   # expo lint (ESLint) + Prettier
npm test                       # jest-expo + React Native Testing Library
npm run gen:api                # regenerate src/api/schema.d.ts from backend OpenAPI
```

## Stack

Expo (latest SDK, managed workflow + dev builds) · TypeScript `strict` · Expo Router (file-based) · NativeWind ·
Reanimated · Victory Native (Skia-based charts) · TanStack Query · expo-secure-store · expo-notifications ·
react-native-view-shot + expo-sharing (recap card export). Don't add a dependency without saying why. Check it supports
the New Architecture and the current Expo SDK.

## Structure

```
mobile/
  app/                       # Expo Router routes ONLY (thin: compose feature components)
    (tabs)/_layout.tsx       # Home, Progress, Wrapped, Activities, Profile
    (tabs)/index.tsx ...
    (auth)/...               # sign in / sign up
    wrapped/[recapId].tsx    # full-screen recap story (outside tabs, modal presentation)
  src/
    api/                     # fetch client, generated schema.d.ts, query keys
    features/<feature>/      # components, hooks (useActivities), utils for one feature
    components/ui/           # shared primitives (Text, Card, StatTile, Screen)
    theme/                   # design tokens: colors, gradients, spacing, typography
    lib/                     # formatting (pace, distance, duration), units, dates
```
- Route files stay thin. Logic lives in `src/features/*`.
- Import alias `@/` → `src/`.

## Conventions

- **Server state = TanStack Query.** Don't copy API data into `useState`/global stores. Query keys live in one place per feature.
  Only add a client-state lib (Zustand) once we actually have cross-screen client state.
- **API types are generated** from the backend OpenAPI spec (`openapi-typescript`). Never hand-write response types.
- **Units:** the API returns meters/seconds. Convert using `src/lib/units.ts` and the user's preferred unit. All pace/distance/duration
  formatting goes through `src/lib/format.ts`. Write unit tests for these.
- **Auth tokens** go in `expo-secure-store` only, never AsyncStorage. The API client handles refresh-on-401 in one place.
- **Styling:** NativeWind classes backed by tokens in `tailwind.config` / `src/theme`. No magic hex colors or one-off spacing in components.
  Dark mode first. The Wrapped experience has its own bolder token set (gradients, display type) separate from the dashboard.
- **Animation:** Reanimated (shared values, `useAnimatedStyle`, layout animations) on the UI thread. Don't animate with `setState`.
  Respect reduced motion (`useReducedMotion`).
- **Lists:** `FlatList`/`FlashList` with stable keys and pagination via `useInfiniteQuery`. Never `.map()` a long list inside a ScrollView.
- **Accessibility:** `accessibilityLabel`s on icon buttons and charts (give a text summary of what the chart shows). Support dynamic type sizes.
- Components are function components with typed props, and they're small. Use `function` declarations for components.
- Handle loading / empty / error states on every data screen. A new user with zero activities is a real, designed state.

## Gotchas

- iOS simulator can reach the backend at `http://localhost:8081` (dev port). A physical device needs your machine's LAN IP. Configure via
  `EXPO_PUBLIC_API_URL` (`EXPO_PUBLIC_*` vars are bundled into the app, so **never** put secrets there).
- Expo Go can't load custom native modules. Once we add any, use a development build (`npx expo run:ios` or EAS).

## Testing

- Unit test pure utils (formatting, units, chart data shaping).
- RNTL for component behavior (renders states, user interactions). Mock the network at the fetch layer, not the hooks.
- Don't snapshot-test animated Wrapped cards. Test the data → card-content mapping instead.
