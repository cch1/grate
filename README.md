# Welcome to The Olympic Games of Code
You will be participating with your teammates against competing teams in a sequence of scored events.  Some events will be scored based on the time it takes your team to respond correctly while others are scored purely on the correctness of your team's submissions.  This file will contain the rules of the events, evolving throughout The Games with pushed changes from the Olympic Committee to document the current event and results of prior events.

## General Rules
* The Olympic Games ("The Games") will occur at irregular intervals.
* The Games are composed of Events.
* Each Event has an Event Name of the format "EventNN".
* In each Event, Teams compete against each other to earn points according to the specific rules of that Event.
* Teams are composed of Competitors.
* The Team with the most points at the end of The Games is the winner.  Winners are entitled to veneration.  They alone reap the rewards provided by the Olympic Committee.
* The Olympic Committee is composed of a Judge and a Scorekeeper, and they are the final arbiter of results and the authoritative interpreter of these rules and any other rules they may chose to impose during the course of The Games.

## Facilities
The Olympic Facilities are made up of the following:

#### Judge Computer
All event solutions will be evaluated on the Judge Computer by the Judge using the `make` command against the `Makefile` in the root of the _Games Repository_.  The environment of the Judge Computer will be documented by an [asdf](https://asdf-vm.com/)-compatible `./.tools-version` file in the root of the _Games Repository_.
#### Competitor Computers
Each Competitor must provide his or her own computer.  You are responsible for being able to adequately mimic the _Judge Computer_ environment to ensure that the Judge can properly evaluate any submitted solutions.
#### Team Repository
Each team will be assigned one git repository for its exclusive use.  A team must push _Event Solution Commits (ESCs)_ to its Team Repsitory, where ESCs represent a Team's solution to an Event.  Beyond hosting ESCs, use of the Team Repository is at the discretion of each Team.  ESCs do not need to persist after an event has been officially scored unless specifically noted in the event's rules.  At the start of The Games each Team Repository shall be empty.  **No Competitor shall push any changes to any Team Repository other than the Team Repository assigned to the Competitor's Team**.
#### Games Respository
The Games Repository contains a _Team Submodule_ for each team (pointing to the `main` branch of its _Team Repository_), this `README.md` and common resources used by all teams.  To "submit" an ESC a team must push an _ESC-Reference Commit_ to the `main` branch of the _Games Repository_.  An ESC-Reference Commit updates that team's submodule to point to the ESC in its _Team Repository_.  ESC-Reference Commits must have a commit message whose title line is the Event ID followed by a space and the Team Name, e.g. `Event07 Stratos`.  **No other commits to the Games Repository shall be permitted and no force pushes are allowed to the Games Repository**.  The Olympic Committee will update the Games Repository during The Games to communicate precise rules for each Event.
#### Games Channel
The Olympic Committee communicates precise written instructions via a conversation in the shared Slack [Games Channel](https://guaranteedrate.slack.com/archives/C02JUBVNG8Y).  A huddle may be used for verbal instructions that may clarify the rules.  Instead of posting messages to the _Games Channel_ competitors should contact the Olympic Committee members via direct message to minimize the noise in this sacred channel.

### Notes on ESCs and ESC-Reference Commits
* ESCs are evaluated by make targets of the form `<EventID>` (e.g. `Event04`) in the `./Makefile` file of the Games Repository.
* The make targets generate output in the `./results/<EventID>` directory.
* The Judge will make the Event target (e.g. `make Event01`) on the Judge computer at the end of each Event.
* The make target will generate a log of ESC Reference Commits in the `log.txt` file of the event results directory.
* Order of submission of ESCs will be determined by commit ancestry (topological order) of the ESC-Reference Commits in the Games Repository.
* The make target may capture output of the ESC evaluation in a file per team in the event results directory.
* Correctness of an ESC may only partially be confirmed by captured output -for some Events the Judge will inspect the output to confirm correctness.
* It is possible to simulate the evaluation of an ESC locally by running the `make <EventID>` command from a local clone of the Games Repository.

## Preparation for The Games
1. Competitors will be randomly assigned to a Team prior to the start of The Games; they remain on that Team for the duration of The Games.
2. Each Team will select a single member Competitor as Team Captain.
3. Each Team Captain will announce a unique Team Name selected from the names of [ancient Greek city-states](https://en.wikipedia.org/wiki/List_of_ancient_Greek_cities).
4. The Olympic Committee will assign a Team Repository to each Team.
5. The Olympic Committee will push a final update to the _Games Repository_ adding Team Submodules for each Team linking a directory (named by the Team Name) to the Team Repository.
