Habit Tracker 
Habit Tracker minimalist Android application designed to help users build lasting routines through data-driven consistency. By combining a sleek, modern interface with a "Hard Mode" streak system, this app provides a powerful way to visualize and maintain daily progress.

Key Features 
Advanced Streak Engine: Implements a rigorous calculation that only rewards "Perfect Days"—days where 100% of active habits are completed.

Calendar: A custom-engineered grid calendar allowing users to jump between months and years to track historical data and view past habit completion rings.

Theme-Adaptive UI: The entire interface, including custom-rounded dialogs and buttons, seamlessly transitions between a crisp Light Mode and a deep, polished Dark Mode.

Local Persistence: Leverages the Room Persistence Library to ensure all habit data, entry history, and user settings are stored securely and locally.

Daily Motivation: Displays a synchronized "Thought of the Day" system that provides encouraging quotes based on the current date.

Technical Stack 
Language: Kotlin.

Database: Room (SQLite abstraction).

Architecture: Coroutines and LifecycleScope for non-blocking database operations.

UI: ConstraintLayout, RecyclerView, and Custom-inflated AlertDialogs.

How it Works 
Personalize: Set your name in the settings for a custom greeting with a smooth fade-in animation upon launch.

Add Habits: Create daily goals using the custom rounded dialogs that match your chosen theme.

Review: Use the Calendar view to see your "Today" pulse and navigate back in time to check previous performance.
