package be.vlaanderen.informatievlaanderen.ldes.server.retention.services.retentionpolicy.execution;

public interface MemberRemover {

	void removeMemberFromView(RetentionCandidate memberProperties, String viewName);
}
