# ViaKaab — Kotlin + Jetpack Compose

Rewritten from the imported Java/XML `astrocodestudio1-dotcom/Viakaab-beta-1.0` repository into a modern Kotlin + Jetpack Compose Android application targeting Android SDK 36.

## Features
- **Authentication & Role Onboarding**: Splash, Login (with remembered credentials & Supabase password reset), Registration, and Role Selection (`Conductor`, `Repartidor`, `Pasajero`, `Ambos`).
- **Passenger Hub (`MainContainerScreen`)**: 4-tab Material 3 navigation (`Inicio`, `Opciones`, `Actividad`, `Cuenta`) with quick trip shortcuts, activity history, profile management, payment options, support, and terms.
- **Interactive Maps & Fare Bidding**:
  - `PassengerMapScreen`: Google Maps Compose integration, runtime location permission (`ACCESS_FINE_LOCATION`), live geocoding & tap-to-pick reverse geocoding with Felipe Carrillo Puerto landmark fallbacks, `Estándar`, `Ciudad a Ciudad`, and `Entregas / Envíos` modes, and `$ MXN` fare offers.
  - `DriverMapScreen`: Availability toggle (`Conectado` / `Desconectado`), nearby request cards, and inDrive-style counter-offer dialog.
- **Driver Onboarding & CameraX Verification**: Vehicle registration, zero-permission `PickVisualMedia` document uploads, and live CameraX selfie + 2-step INE verification (`CAMERA` runtime permission).
- **ViaKaab AI Assistant**: In-app conversational assistant (`GeminiChatScreen`) routed through `AiRepository` (currently using `PlaceholderAiRepository` pending Firebase AI Logic confirmation).

## Configuration
- **Google Maps API Key**: Set `MAPS_API_KEY` in the **Secrets panel in AI Studio** (do not use `local.properties`).
- **AI Assistant**: Uses Firebase AI Logic (`Firebase.ai` + App Check) once Firebase setup is confirmed in the UI; no `GEMINI_API_KEY` is required in `.env.example`.
