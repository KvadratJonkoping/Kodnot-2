import { Participant } from "./result-parser";

export function createTimeTeams(
    remainingMembers: Array<Participant>,
    teamNumber: number = 1,
    timeTeams: { [key: string]: Array<Participant> } = {}): { [key: string]: Array<Participant> } {
    if (remainingMembers.length === 0) {
        return timeTeams;
    }

    const teamId = getTeamId(teamNumber, timeTeams);
    const teamKey = getTeamKey(teamId);
    const currentMembers = timeTeams[teamKey] || [];
    const predicate = getPredicate(currentMembers);
    const nextTeamMember = remainingMembers.find(predicate);

    if (!nextTeamMember) {
        return timeTeams;
    }

    return createTimeTeams(
        remainingMembers.filter((rm) => rm !== nextTeamMember),
        teamId,
        { ...timeTeams, [teamKey]: currentMembers.concat(nextTeamMember) }
    );
}

export function filterValidTimeTeams(timeTeams: { [key: string]: Array<Participant> }): { [key: string]: Array<Participant> } {
    return Object.keys(timeTeams)
        .reduce((soFar, current) => {
            if (timeTeams[current].length === 3) {
                return {
                    ...soFar,
                    [current]: timeTeams[current]
                }
            }

            return soFar;
        }, {});
}

function getTeamId(teamNumber: number = 1, timeTeams: { [key: string]: Array<Participant> }) {
    const currentTeamKey = getTeamKey(teamNumber);
    return (timeTeams[currentTeamKey] || []).length === 3 ? (teamNumber + 1) : teamNumber;
}

function getTeamKey(id: number): string {
    return `team-${id}`;
}

function getPredicate(currentMembers: Array<Participant>): (participant: Participant) => boolean {
    if (currentMembers.filter((m) => m.gender === "male").length > 1) {
        return (participant: Participant) => participant.gender === "female";
    } else if (currentMembers.filter((m) => m.gender === "female").length > 1) {
        return (participant: Participant) => participant.gender === "male";
    }

    return () => true;
}