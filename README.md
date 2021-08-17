### The Hosted Gaming Discord bot project.
By [Heiko Ribberink](https://github.com/HeikoRibberink) and [Micha Pehlivan](https://github.com/MichaPehlivan)

---

The purpose of this project is to code a Discord bot with the following functionality:

- ~~The bot should be able to join and leave a voice chat and stream a specified application running on the host computer.~~ *It is currently impossible for bots to go live.*
- The bot should be able to recieve a specified range of inputs through Discord from an unlimited number of users and convert them to input events for the application on the host computer.
- The bot should be safe to run on the host computer **under supervision** of a human. It should not be able to create unspecified input events, and should **ALWAYS** be able to safely be killed.
- All configuration should be done through configuration files that can be linked to the program through arguments (or another configuration file) for better end-user experience.
-The bot should follow the Reactive Streams specifications.

---

