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
The Olympic Committee communicates precise written instructions via a conversation in the shared Slack [Games Channel](https://guaranteedrate.slack.com/archives/C02LCDD6SPR).  A huddle may be used for verbal instructions that may clarify the rules.  Instead of posting messages to the _Games Channel_ competitors should contact the Olympic Committee members via direct message to minimize the noise in this sacred channel.

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

## Events

### Event01: The ESC Dash
Objective: Submit a [possibly empty](https://git-scm.com/docs/git-commit#Documentation/git-commit.txt---allow-empty)) ESC.

Scoring:
	* The first team to submit an ESC shall be awarded 2 points.
	* The second team to submit an ESC shall be awarded 1 point.

Time Limit: 2 minutes

Additional Rules and Notes:
1.  For this event, correctness is represented solely by inclusion in the log generated by `make Event01`.

### Event02: The ESC Relay
Objective: Push a [possibly empty](https://git-scm.com/docs/git-commit#Documentation/git-commit.txt---allow-empty) "Relay Commit".  Recursively, the next commit must also be a Relay Commit.  Stop after four levels of recursion (four commits).  Each Relay Commit must have a different Author than the previous commit and the commit message must be the same as the ESC-Reference Commit (Event ID followed by Team Name).  All Competitors on a Team must author at least one commit in the sequence of four commits.  The final Relay Commit will be the ESC.

Scoring:
	* The first team to submit a correct ESC shall be awarded 4 points.
	* The second team to submit a correct ESC shall be awarded 2 points.
	* All other teams submitting a correct ESC shall be awarded 1 point.

Time Limit: 5 minutes

Additional Rules and Notes:

1. Correctness is determined by the Judge's evaluation of the output of the `make Event02` command run on the Judge Computer from the root of the Game Repository.

## Event03: The CSV Reader
Objective: Submit an executable file `Event03` in the root of your Team Repository that reads CSV text (with a header line) provided on standard input and the following command line parameters:

1. a field name (as defined in the header line) as the first command line parameter
2. a record number as the second command line parameter.  Consider the header line to be record zero.

Emit to standard output the value of the given field for the given record index as a quoted string.

Scoring:
	* The first team to submit a correct ESC shall be awarded 8 points.
	* The second team to submit a correct ESC shall be awarded 4 points.
	* All other teams submitting a correct ESC shall be awarded 2 points.

Time Limit: 20 minutes

Additional Rules and Notes:

1. Correctness is determined by the Judge's evaluation of the output of the `make Event03` command run on the Judge Computer from the root of the Game Repository.
2. The data provided on standard input and the environment vars `FIELD` and `ROW` that parameterize the evalation are not revealed until the Judge makes the `Event03` target while officially scoring the event.
3. There is a sample CSV file in the Games Repository.  The output of `cat resources/Event03-Sample.csv | make Event03 namelast 10` should be `"Rozenberg"`.

## Event04: The ATP Results Accumulator
Objective: Submit an executable file `Event04` in the root of your Team Repository that reads CSV text (with a header line) of ATP Tennis Match Results provided on standard in and emits a summary of the data on standard
out.  The input format is described [here](https://github.com/JeffSackmann/tennis_atp/blob/master/matches_data_dictionary.txt).  The summary to be emitted should be as follows:

```
EpochStart, EpochEnd, AverageMatchesPerWeek, MaxMatchesPerWeek, MostActivePlayer
```

* The `EpochStart` entry is the stringified date in YYYY-MM-DD format of the Monday of the week of the earliest tournament_start date in the database.
* The `EpochEnd` entry is the stringified date in YYYY-MM-DD format of the Monday of the week of the latest tournament_start date in the database.
* The `AverageMatchesPerWeek` is the integer total number of match results in the database divided by the number of weeks in the epoch.  Note that some weeks may have zero matches.
* The `MaxMatchesPerWeek` is the greatest number of matches played in any week during the epoch.
* The `MostActivePlayer` is the player who competes in the most matches (as winner or loser) during the epoch.

Scoring:
	* The first team to submit a correct ESC shall be awarded 16 points.
	* The second team to submit a correct ESC shall be awarded 8 points.
	* All other teams submitting a correct ESC shall be awarded 4 points.

Time Limit: 50 minutes.

Additional Rules and Notes:

1. Correctness is determined by the Judge's evaluation of the output of the `make Event04` command run on the Judge Computer from the root of the Game Repository.
2. The data provided on standard input that parameterize the evalation is not revealed until the Judge makes the `Event04` target while officially scoring the event.
3. There is a sample CSV file in the Games Repository.  The output of `cat resources/Event04-Sample.csv | make Event04` should be `Rozenberg`.
