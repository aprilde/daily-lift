package com.dailylift.app.data

import java.util.UUID

/**
 * Default Monday-Friday workout content, ported from `workout-widget-prototype.html`'s
 * `DEFAULT_DATA`, with "AMRAP" replaced by "as many as you can" and a generated [Exercise.id]
 * and image-resource-name pair added to every exercise.
 */
fun createSeedWorkoutData(): WorkoutData = mapOf(
    Weekday.MONDAY to WorkoutDay(
        focus = "Lower body",
        exercises = listOf(
            seedExercise("Goblet squat", "", "8-12 reps", "Hold the weight at your chest, sit back like into a chair, knees tracking over toes."),
            seedExercise("Dumbbell RDL", "", "8-12 reps", "Hinge at the hips with a flat back, lower the weights along your legs, feel it in your hamstrings."),
            seedExercise("Reverse lunges", "", "8-10 ea leg", "Step backward, drop your back knee toward the floor, push through your front heel to stand."),
            seedExercise("Split squats", "", "8-10 ea leg", "Keep most of your weight on your front foot, lower straight down, stay tall."),
            seedExercise("Glute bridge", "", "12-15 reps", "Lie on your back, drive through your heels, squeeze your glutes at the top."),
            seedExercise("Standing calf raises", "", "12-20 reps", "Rise onto your toes slowly, pause at the top, lower with control."),
            seedExercise("Dead bugs", "bodyweight", "10-12 ea side", "Press your lower back into the floor, extend opposite arm and leg slowly."),
        ),
    ),
    Weekday.TUESDAY to WorkoutDay(
        focus = "Upper body push",
        exercises = listOf(
            seedExercise("Dumbbell floor press", "", "8-12 reps", "Lie on your back, press the weights straight up, lower until your elbows touch the floor."),
            seedExercise("Shoulder press", "", "8-12 reps", "Press the weights overhead, keep your core tight, don't arch your back."),
            seedExercise("Push-ups", "bodyweight", "as many as you can", "Keep a straight line from head to heels. Drop to your knees if needed, that's totally fine."),
            seedExercise("Lateral raises", "", "12-15 reps", "Lift the weights out to your sides to shoulder height, slight bend in elbows."),
            seedExercise("Tricep extensions", "", "10-15 reps", "Keep your elbows pointing forward, only your forearms move."),
            seedExercise("Front raises", "", "10-12 reps", "Raise the weights straight in front to shoulder height, lower slowly."),
            seedExercise("Leg raises", "bodyweight", "10-12 reps", "Keep your legs straight, lower them slowly without arching your back off the floor."),
        ),
    ),
    Weekday.WEDNESDAY to WorkoutDay(
        focus = "Lower body",
        exercises = listOf(
            seedExercise("Dumbbell RDL", "", "8-12 reps", "Hinge at the hips with a flat back, feel the stretch in your hamstrings."),
            seedExercise("Sumo dumbbell squat", "", "10-12 reps", "Wide stance, toes pointed out, hold one weight between your legs and squat down."),
            seedExercise("Single-leg RDL", "", "8-10 ea leg", "Balance on one leg, hinge forward, extend the other leg behind you. Go slow."),
            seedExercise("Side lunges", "", "8-10 ea side", "Step out to the side, bend that knee, keep the other leg straight."),
            seedExercise("Glute bridge march", "bodyweight", "10 ea side", "Hold a bridge, then lift one knee at a time without dropping your hips."),
            seedExercise("Wall sit", "bodyweight", "30-60 sec", "Back flat against the wall, slide down until your thighs are parallel to the floor."),
            seedExercise("Russian twists", "", "10-12 ea side", "Lean back slightly, rotate your torso side to side, keep your core engaged."),
        ),
    ),
    Weekday.THURSDAY to WorkoutDay(
        focus = "Upper body pull",
        exercises = listOf(
            seedExercise("Bent-over row", "", "8-12 reps", "Hinge forward with a flat back, pull the weights to your ribs, squeeze your shoulder blades."),
            seedExercise("Wide bent-over row", "", "8-12 reps", "Same as a row but pull with elbows wider, targets your upper back."),
            seedExercise("Reverse fly", "", "12-15 reps", "Bent over, raise the weights out to your sides, squeeze your shoulder blades together."),
            seedExercise("Bicep curls", "", "10-15 reps", "Keep your elbows pinned to your sides, curl the weights up slowly."),
            seedExercise("Hammer curls", "", "10-15 reps", "Like a curl but palms face each other, like holding hammers."),
            seedExercise("Shrugs", "", "12-15 reps", "Lift your shoulders straight up toward your ears, pause, lower slowly."),
            seedExercise("Plank", "bodyweight", "30-60 sec", "Straight line head to heels, squeeze your core, don't let your hips sag."),
        ),
    ),
    Weekday.FRIDAY to WorkoutDay(
        focus = "Full body",
        exercises = listOf(
            seedExercise("Squat to press", "", "8-10 reps", "Squat down, then as you stand press the weights overhead in one smooth move."),
            seedExercise("Dumbbell deadlift", "", "8-12 reps", "Weights in front of your legs, hinge and lower, drive through your heels to stand."),
            seedExercise("Renegade row", "", "6-10 ea side", "In a plank holding weights, row one up at a time, keep your hips steady."),
            seedExercise("Walking lunges", "", "10 ea leg", "Step forward into a lunge, then bring the back leg through into the next step."),
            seedExercise("Floor press", "", "8-12 reps", "Lie down, press the weights up, lower until your elbows touch the floor."),
            seedExercise("Mountain climbers", "bodyweight", "10-12 ea side", "In a plank, drive your knees toward your chest one at a time, quick but controlled."),
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
