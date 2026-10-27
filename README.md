# UMBC Macro

Send chat messages and commands with a key combo. Bind `/home`, `/spawn`, `/warp mine` or a quick "gg" to any key, optionally with Ctrl, Alt or Shift, and press it to send. Made for the UMBC Minecraft server, works anywhere.

- Any number of macros, each with a key and an optional Ctrl / Alt / Shift modifier
- Text starting with `/` is sent as a command, anything else as a chat message
- Edit macros in game: press **J** (rebindable), run `/umbcmacro`, or open it from Mod Menu
- Settings are saved to `config/umbcmacro.json`

Requires Fabric API and Mod Menu. Client side only; servers don't need it.

Check that macros are allowed on the server you play on.

## Credits

UMBC Macro is a 26.3 port of [Chat Macro](https://modrinth.com/mod/chat-macro) by **spunkyinsaan**, used under the MIT license. The 26.3 version reads keys through Minecraft's new SDL3 input layer and has a new look and name.

## Building

```
./gradlew build
```

The jar is in `build/libs/`.

## License

MIT. See [LICENSE](LICENSE) (original copyright spunkyinsaan).
