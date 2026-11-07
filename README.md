# FGM Body Shape (Forge 1.20.1): waist, hips, glutes

Addon for Female Gender Mod. Standalone: own data (config/fgmbody.json), own network channel
(fgmbody:main), own render layer. No mixins; FGM / FGM Plus code is not modified, so it coexists with FGM Plus.

Build:   gradle wrapper (once), then ./gradlew build  ->  build/libs/
Dev run: ./gradlew runClient
In game: press J (or the "Body shape" button in FGM's wardrobe), move sliders, press Done.
Put the mod on the server too (with FGM) so other players see your shape.

Waist: the vanilla torso + jacket are hidden while waist > 0 and redrawn as 5 slices (TorsoEvents + BodyShapeLayer).
