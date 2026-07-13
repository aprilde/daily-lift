package com.dailylift.app.data

import java.util.UUID

/**
 * Default Monday-Friday workout content, ported from `workout-widget-prototype.html`'s
 * `DEFAULT_DATA`, with a generated [Exercise.id] and image-resource-name pair added to every
 * exercise. Reps are single short values to fit the narrow reps field: ranges collapsed to their
 * upper bound (e.g. "8-12 reps" -> "12"), per-side qualifiers dropped ("8-10 ea leg" -> "10"),
 * time-based holds keep a unit ("30-60 sec" -> "60 sec") so they aren't misread as a rep count,
 * and Push-ups' open-ended AMRAP got a concrete target ("15").
 */
fun createSeedWorkoutData(): WorkoutData = mapOf(
    Weekday.MONDAY to WorkoutDay(
        focus = "Lower body",
        exercises = listOf(
            seedExercise("Goblet squat", "", "12", "Hold the weight at your chest, sit back like into a chair, knees tracking over toes."),
            seedExercise("Dumbbell RDL", "", "12", "Hinge at the hips with a flat back, lower the weights along your legs, feel it in your hamstrings."),
            seedExercise("Reverse lunges", "", "10", "Step backward, drop your back knee toward the floor, push through your front heel to stand."),
            seedExercise("Split squats", "", "10", "Keep most of your weight on your front foot, lower straight down, stay tall."),
            seedExercise("Glute bridge", "", "15", "Lie on your back, drive through your heels, squeeze your glutes at the top."),
            seedExercise("Standing calf raises", "", "20", "Rise onto your toes slowly, pause at the top, lower with control."),
            seedExercise("Dead bugs", "bodyweight", "12", "Press your lower back into the floor, extend opposite arm and leg slowly."),
        ),
    ),
    Weekday.TUESDAY to WorkoutDay(
        focus = "Upper body push",
        exercises = listOf(
            seedExercise("Dumbbell floor press", "", "12", "Lie on your back, press the weights straight up, lower until your elbows touch the floor."),
            seedExercise("Shoulder press", "", "12", "Press the weights overhead, keep your core tight, don't arch your back."),
            seedExercise("Push-ups", "bodyweight", "15", "Keep a straight line from head to heels. Drop to your knees if needed, that's totally fine."),
            seedExercise("Lateral raises", "", "15", "Lift the weights out to your sides to shoulder height, slight bend in elbows."),
            seedExercise("Tricep extensions", "", "15", "Keep your elbows pointing forward, only your forearms move."),
            seedExercise("Front raises", "", "12", "Raise the weights straight in front to shoulder height, lower slowly."),
            seedExercise("Leg raises", "bodyweight", "12", "Keep your legs straight, lower them slowly without arching your back off the floor."),
        ),
    ),
    Weekday.WEDNESDAY to WorkoutDay(
        focus = "Lower body",
        exercises = listOf(
            seedExercise("Dumbbell RDL", "", "12", "Hinge at the hips with a flat back, feel the stretch in your hamstrings."),
            seedExercise("Sumo dumbbell squat", "", "12", "Wide stance, toes pointed out, hold one weight between your legs and squat down."),
            seedExercise("Single-leg RDL", "", "10", "Balance on one leg, hinge forward, extend the other leg behind you. Go slow."),
            seedExercise("Side lunges", "", "10", "Step out to the side, bend that knee, keep the other leg straight."),
            seedExercise("Glute bridge march", "bodyweight", "10", "Hold a bridge, then lift one knee at a time without dropping your hips."),
            seedExercise("Wall sit", "bodyweight", "60 sec", "Back flat against the wall, slide down until your thighs are parallel to the floor."),
            seedExercise("Russian twists", "", "12", "Lean back slightly, rotate your torso side to side, keep your core engaged."),
        ),
    ),
    Weekday.THURSDAY to WorkoutDay(
        focus = "Upper body pull",
        exercises = listOf(
            seedExercise("Bent-over row", "", "12", "Hinge forward with a flat back, pull the weights to your ribs, squeeze your shoulder blades."),
            seedExercise("Wide bent-over row", "", "12", "Same as a row but pull with elbows wider, targets your upper back."),
            seedExercise("Reverse fly", "", "15", "Bent over, raise the weights out to your sides, squeeze your shoulder blades together."),
            seedExercise("Bicep curls", "", "15", "Keep your elbows pinned to your sides, curl the weights up slowly."),
            seedExercise("Hammer curls", "", "15", "Like a curl but palms face each other, like holding hammers."),
            seedExercise("Shrugs", "", "15", "Lift your shoulders straight up toward your ears, pause, lower slowly."),
            seedExercise("Plank", "bodyweight", "60 sec", "Straight line head to heels, squeeze your core, don't let your hips sag."),
        ),
    ),
    Weekday.FRIDAY to WorkoutDay(
        focus = "Full body",
        exercises = listOf(
            seedExercise("Squat to press", "", "10", "Squat down, then as you stand press the weights overhead in one smooth move."),
            seedExercise("Dumbbell deadlift", "", "12", "Weights in front of your legs, hinge and lower, drive through your heels to stand."),
            seedExercise("Renegade row", "", "10", "In a plank holding weights, row one up at a time, keep your hips steady."),
            seedExercise("Walking lunges", "", "10", "Step forward into a lunge, then bring the back leg through into the next step."),
            seedExercise("Floor press", "", "12", "Lie down, press the weights up, lower until your elbows touch the floor."),
            seedExercise("Mountain climbers", "bodyweight", "12", "In a plank, drive your knees toward your chest one at a time, quick but controlled."),
        ),
    ),
)

private fun seedExercise(name: String, weight: String, reps: String, tip: String): Exercise {
    val slug = name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
    return Exercise(
        id = UUID.randomUUID().toString(),
        name = name,
        weight = weight,
        reps = reps,
        tip = tip,
        imageStartRef = "${slug}_start",
        imageEndRef = "${slug}_end",
    )
}
