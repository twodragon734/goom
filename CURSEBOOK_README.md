    # Curse Book

Fabric 1.21.1 mod that adds a multiplayer curse book.

## Features

- Craft the book with 4 gold ingots, 4 paper, and 1 book.
- Right-click while holding the book to open the curse picker.
- Choose a curse, then choose another online player.
- Available curses: Weakness, Darkness, Mining Fatigue, Hunger, and Slowness.
- Curses last for a limited time and can be removed with `/cursebook cleanse <player>`.

The current implementation allows any player to use the book, as requested.

## Build without installing anything

You can build this on GitHub's servers. You do not need Java or Gradle installed on your PC.

1. Create a new GitHub repository.
2. Upload all of these project files, including the `.github\workflows\build.yml` file.
3. Open the repository's **Actions** tab.
4. Open **Build Curse Book mod** and click **Run workflow**.
5. When it finishes with a green check, open the workflow run.
6. Download the artifact named **cursebook-fabric-1.21.1**.
7. Extract the downloaded ZIP. The `.jar` inside is the mod.

The workflow installs Java 21 and Gradle temporarily on GitHub's build server. Nothing is installed on your computer.

Install Fabric Loader 0.16.5 or newer, Fabric API for Minecraft 1.21.1, and Java 21 on the server and every client.

## In-game usage

1. Craft or give yourself the item with `/give @s cursebook:curse_book`.
2. Hold it in your main hand and right-click.
3. Pick a curse and a player.
4. To cleanse a player, run `/cursebook cleanse <player>`.
