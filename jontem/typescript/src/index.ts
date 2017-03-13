import { Participant, parseParticipants } from "./result-parser";
import { createTimeTeams, filterValidTimeTeams } from "./time-teams";

function groupByClub(participants: Array<Participant>) {
    return participants.reduce((soFar, current) => {
        if (soFar[current.team]) {
            return {
                ...soFar,
                [current.team]: soFar[current.team].concat(current)
            }
        }

        return { ...soFar, [current.team]: [current] };
    }, {});
}

const participants: Array<Participant> = parseParticipants("../../resultat.csv")
    .sort((a, b) => a.time - b.time);

const participantByClub = groupByClub(participants);

const result = Object.keys(participantByClub).reduce((soFar, currentClub) => {
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
    console.log(`-------- end ---------`);
}