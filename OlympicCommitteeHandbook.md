## Preparation for The Games

### One Day Before the start of The Games

#### Create Team Repositories
Judge creates the Team Repositories.  The names of the repositories should be arbitrary.  All Competitors should have complete access to each repository.

#### Create the Games Channel
Judge creates the Games Channel as a private channel.  Judge administratively invites the other members of the Olympic Committee to the Games Channel.

### Approximately 30 minutes before the start of The Games

#### Open Games Channel
Scorekeeper administratively invites all Competitors to the channel.

#### Announcement
Scorekeeper announces the Games Channel coordinates to the Competitors and confirms the start time of The Games with a rendez-vous in the Games Channel for Opening Ceremonies.

### Opening Ceremonies

#### Confirm Attendance
Scorekeeper establishes a audo communication with all Competitors and the Olympic Committee and confirms attendance.

#### Opening Statement
Judge reminds Competitors to prepare their Competitor Computers (e.g. preparing meeting equipment, opening shells, cloning the Games Repository, starting IDEs, etc) and to be prepared to receive instructions via the `README.md` in the Games Repository.
Scorekeeper reminds Competitors that Games Channel posts will be used to communicate event status
Scorekeeper reminds Competitors that they may drop from the Games Channel audio to caucus with their Team after each Event has started but that Competitors will be recalled to the audio channel at the end of each Event.

#### Assign Competitors randomly to Teams
Judge runs a script to randomly assign Competitors and a Team Repository to Teams:
``` clojure
(zipmap [:red :orange :yellow :green :blue :indigo :violet]
              (partition-all 3 (shuffle ["Travis" "RC" "James" "Paco" "Bret" "Randy" "Zach" "Proctor" "Burin" 'Steve' "Andrew" "Mark"])))
```
Scorekeeper announces the assignment of Competitors and a Team Repository to each Team.

#### Establish Team Communication
Scorekeeper announces a one minute period for teams to setup private communication via a group DM in Slack.  At the end of the period, Scorekeeper posts to the Games Channel and recalls competitors to the Games Channel audio.

#### Identify Team Captains and Team Names
Scorekeeper announces a four minute period of caucus after which Team Captains must identify themselves and their Team Name in the Games Channel via a post.

#### Update Games Repository
Scorekeeper announces short pause for updating Games Repository.
Judge updates the Games Repository adding a submodule per Team linked to a Team Repository.  Commit, Push, Announce, Confirm.
```bash
git submodule add —name teamName teamRepositoryURL teamName
```
Scorekeeper declares the end of the preparation phase and the beginning of the competition phase.

## The Games
For each Event the Olympic Committee follows this script:
1. Scorekeeper announces the Event name, number and time limit (if any).
2. Judge updates the Games Repository as follows:
   1. Add EventNN Rules to `README.md`.
   2. Add EventNN `Makefile` entries.
3. Judge commits, pushes and simultaneously triggers Scorekeeper.
4. Scorekeeper starts timer (as needed) and announces the start of the Event.
5. Competitors compete in the Event by pulling the updated Games Repository, reading the event rules and eventually submitting their solution (or timing out).
6. At infrequent intervals (to minimize interruptions) Scorekeeper announces the remaining time by posting in the Games Channel.
7. At the end of the allotted time, Scorekeeper announces the end of the event and triggers Judge.  Scorekeeper recalls Competitors to the Games Channel audio.
8. Judge pulls the `main` branch of the Games Repository and evaluates the solutions.
9. Judge announces scores for the Event.
10. Scorekeeper announces and posts the running scoreboard.

## Event Notes
### Event03
``` bash
cat resources/Event03.csv | make Event03 namelast 11
```
ANSWER: "Reilly Opelka"

## Closing Ceremonies
Scorekeeper announces the final scores.
Scorekeeper awards prizes to the winning Team.
Olympic Committee solicits input for future Hackathons

FIN
