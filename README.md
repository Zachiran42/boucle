# Boucle

[Télécharger Boucle.apk](https://github.com/Zachiran42/boucle/releases/latest/download/Boucle-1.1.0.apk) · [Versions](https://github.com/Zachiran42/boucle/releases) · [Les 50 niveaux](NIVEAUX.md)

Jeu Android de dessin et de réflexes créé pour Cercle. Trace une boucle autour des points lumineux et garde les éclats corail à l’extérieur.

## Une campagne de 50 niveaux

Cinq chapitres de dix niveaux, avec des configurations et des objectifs définis :

1. Premiers gestes : découvrir la capture et les premiers obstacles.
2. Prendre de l’élan : capturer en groupe et enchaîner les réussites.
3. Le trait danse : plus d’éclats et des objectifs combinés.
4. Maîtriser la boucle : précision, grandes captures et longues séries.
5. Le grand cercle : les dix derniers défis, jusqu’au niveau 50.

Chaque niveau demande un total de points capturés dans un temps limité, parfois avec un groupe minimum dans une seule boucle ou une série de captures réussies. Les objectifs sont visibles avant et pendant la partie. Le niveau suivant se débloque après une victoire. Les niveaux déjà débloqués restent rejouables.

Une victoire donne une étoile. Conserver au moins deux vies donne une étoile supplémentaire ; terminer avec au moins 35 % du temps en donne une autre. La meilleure note est conservée, jusqu’à150 étoiles. La progression est sauvegardée sur le téléphone.

Le **mode libre** reste disponible : parties de60 secondes, trois vies, combos et record local. Son record est conservé lors de la mise à jour depuis 1.0.0.

## Jouer

Pose un doigt dans le terrain, entoure des points clairs, puis reviens au départ pour fermer la boucle. Un éclat corail touché par le trait ou enfermé coûte une vie. Une boucle vide ou un éclat remet la série courante à zéro. Une fois un objectif de série atteint, il reste validé pour ce niveau.

Le bouton de pause permet de reprendre ou recommencer. Quitter l’appli suspend la partie. Sons désactivables et aide intégrée.

Jeu hors ligne, sans compte, publicité, achat ni permission Android. Android 8.0 ou ultérieur. APK universel.

## Installer et mettre à jour dans Cercle

Télécharger l’APK signé depuis les Releases, puis suivre la confirmation Android. Pour conserver les données, installer la nouvelle version par-dessus l’ancienne, sans désinstaller.

Dans [Cercle](https://github.com/Zachiran42/cercle-store), actualiser le catalogue pour retrouver Boucle. L’application peut aussi être ajoutée avec «Ajouter depuis GitHub» et le lien de ce dépôt. Un ajout personnel reste sur ce téléphone ; le catalogue partagé est géré par le propriétaire du store.

Identité : fr.cercle.boucle. Version1.1.0, versionCode 2. Les mises à jour conservent le même identifiant et certificat, avec un versionCode supérieur. Effacer les données ou désinstaller supprime la progression locale et le record.

## Compiler sous Windows

Outils : JDK 21, Android SDK Platform 36, Build Tools 36.0.0, PowerShell 5.1+. Compilation directe, sans Gradle ni bibliothèque externe.

Copier .toolchain.example.json en .toolchain.json et renseigner javaHome, androidJar et buildTools. Fournir une clé conservée hors du dépôt et charger son mot de passe dans BOUCLE_KEYSTORE_PASSWORD, sans inscrire de secret dans les sources. Mot de passe du magasin et de la clé identiques pour le script.

```powershell
.\scripts\test.ps1
.\scripts\build.ps1 -KeyStore 'C:\cles-privees\boucle.p12' -KeyAlias boucle
```

Options : -ToolchainFile, -BuildDir et -OutputFile. L’APK produit par défaut est build/Boucle-1.1.0.apk. Le script compile, aligne, signe et vérifie la signature et le SHA-256.

## Sources et vérification

- LevelSpec.java : les50 configurations et leurs noms.
- GameEngine.java : géométrie, captures, objectifs et règles.
- CampaignProgress.java : déblocages et meilleures étoiles.
- MainActivity.java et GameView.java : navigation native, sélection et tracé.
- tests/ : régressions du mode libre et simulation déterministe des50 niveaux.

Voir [VALIDATION.md](VALIDATION.md). Les tests et rendus JVM ne remplacent pas un essai tactile sur appareil Android réel.

Code et visuels sous licence [MIT](LICENSE).