import Group from "@/components/Group";


interface GroupListProps {
    groups: Group[];
}

const GroupList = ({ groups }: GroupListProps) => {
    return (
        <div>
            {groups.map((group: Group) => (
                <Group key={group.id} group={group} />
            ))}
        </div>
    );
}


export default GroupList