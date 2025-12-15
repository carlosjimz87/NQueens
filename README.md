# ♛ N-Queens Android App

An elegant Android implementation of the classic **N-Queens puzzle**, built with modern Android architecture, Jetpack Compose, and a strong focus on clean code, testability, and separation of concerns.

The app allows users to play, solve, and track their performance across different board sizes, featuring smooth animations, sound effects, and persistent leaderboards.

---

## 🎥 Demo

<div class="video-container">
  <video autoplay loop muted playsinline width="33%">
    <source src="images/1.webm" type="video/webm">
    Your browser does not support the video tag.
  </video>
  <video autoplay loop muted playsinline width="33%">
    <source src="images/2.webm" type="video/webm">
    Your browser does not support the video tag.
  </video>
  <video autoplay loop muted playsinline width="33%">
    <source src="images/3.webm" type="video/webm">
    Your browser does not support the video tag.
  </video>
</div>

---

## ✨ Features

- 🧩 Interactive N-Queens board (from 4×4 up to 20×20)
- 👑 Conflict detection and visual feedback
- 🎉 Win animation when the puzzle is solved
- ⏱️ Game timer and move counter
- 🏆 Persistent leaderboards per board size
- 🔊 Sound effects for actions and game states
- 🧪 Extensive unit test coverage
- 🧱 Clean, modular, and test-friendly architecture

---

## 🛠 Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **MVVM Architecture**
- **Kotlin Coroutines & Flow**
- **Koin** (Dependency Injection)
- **Jetpack DataStore (Preferences)**
- **kotlinx.serialization**
- **MediaPlayer** (sound effects)

---

## 🧠 Architecture Overview

The project is designed with **clear separation of concerns** and testability as first-class goals.

### Modules

#### `app`
Android-specific layer:
- Jetpack Compose UI
- `BoardViewModel`
- Sound effects
- Persistence and repositories
- Dependency Injection configuration

#### `rules`
Pure Kotlin module:
- `NQueensSolver`
- Conflict detection and validation
- Immutable domain models (`GameState`, `GameStatus`, etc.)

This module has **no Android dependencies**, which allows fast and deterministic unit testing.

---

## 🧩 Core Components

### `BoardViewModel`
Central orchestrator of the app:
- Exposes UI state via `StateFlow`
- Emits one-off UI events via `SharedFlow`
- Coordinates solver, timer, and statistics
- Manages board lifecycle phases (normal play, win animation, frozen state)

### `NQueensSolver`
- Handles queen placement and removal
- Detects conflicts
- Determines when the board is solved
- Produces immutable `GameState` instances

### `GameTimer`
Coroutine-based timer abstraction:
- Emits elapsed time as `StateFlow`
- Supports start, stop, and reset
- Easily replaceable with a fake implementation for testing

### `StatsRepository`
- Records completed games
- Applies deduplication rules
- Computes rankings per board size
- Produces leaderboards sorted by time and moves

### `StoreManager`
Generic persistence abstraction over DataStore:
- Uses `kotlinx.serialization`
- Gracefully handles corrupted data
- Fully unit-testable without Robolectric

---

## 🧪 Testing Strategy

The project emphasizes **fast, deterministic unit tests**:

- Solver logic tested in isolation (pure Kotlin)
- ViewModel tested with fake dependencies
- DataStore tested using temporary files (no Robolectric)
- Coroutine-based logic tested with test dispatchers
- UI behavior driven entirely by state

Run tests with:

```bash
./gradlew test
```

---

## 🚀 Build & Run

### Clone the repository

```bash
git clone <repository-url>
cd nqueens-android
```

### Build the project

```bash
./gradlew build
```

### Run on device or emulator

```bash
./gradlew installDebug
```

---

## 📐 Design Principles

- Single source of truth for state
- Immutable domain models
- Unidirectional data flow
- No business logic inside composables
- Clear separation between UI, domain, and data layers
- Practical testability with high coverage
- Modular architecture with reusable components
