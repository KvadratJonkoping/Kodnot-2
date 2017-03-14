import { Participant } from "./types";
import { parseParticipants } from "./result-parser";
import { createTimeTeams, filterValidTimeTeams } from "./time-teams";
import { formatTime, sumTime } from "./time";

function groupByClub(participants: Array<Participant>) {
    return participants.reduce((soFar, current) => {
        return soFar[current.team]
            ? { ...soFar, [current.team]: soFar[current.team].concat(current) }
            : { ...soFar, [current.team]: [current] };
    }, {});
}

const participants: Array<Participant> = parseParticipants("../../resultat.csv")
    .sort((a, b) => a.time - b.time);

const participantByClub = groupByClub(participants);

const result: { [teamId: string]: Array<Participant> } = Object.keys(participantByClub).reduce((soFar, currentClub) => {
    const timeTeams = createTimeTeams(participantByClub[currentClub]);
    const validTeams = filterValidTimeTeams(timeTeams);
    return Object.keys(validTeams)
        .reduce((soFar, currentTeam) => {
            return {
                ...soFar,
                [`${currentClub}-${currentTeam}`]: validTeams[currentTeam]
            };
        }, soFar);
}, {});

for (const clubKey of Object.keys(result)) {
    const club = result[clubKey];
    console.log(`-------- ${clubKey} ---------`);
    console.log(club);
    console.log(`clubtime: ${formatTime(sumTime(club))}`);
    console.log(`-------- end ---------`);
}