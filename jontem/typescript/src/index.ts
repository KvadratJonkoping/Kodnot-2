import { Participant, parseParticipants } from "./result-parser";
import { createTimeTeams, filterValidTimeTeams } from "./time-teams";

function groupByClub(participants: Array<Participant>) {
    return participants.reduce((soFar, current) => {
        return soFar[current.team]
            ? { ...soFar, [current.team]: soFar[current.team].concat(current) }
            : { ...soFar, [current.team]: [current] };
    }, {});
}

function sum(teamMembers: Array<Participant>) {
    return teamMembers.reduce((soFar, current) => (soFar + current.time), 0);
}

function formatTime(numerical: number): string {
    const minutes = Math.floor(numerical / 60);
    const remaining = parseInt(Math.round(numerical % 60 * 100).toString()) / 100; // Round remaining part with 2 decimals. The JavaScript way :)

    return `${minutes}min ${remaining}sec`;
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
    console.log(`clubtime: ${formatTime(sum(club))}`);
    console.log(`-------- end ---------`);
}