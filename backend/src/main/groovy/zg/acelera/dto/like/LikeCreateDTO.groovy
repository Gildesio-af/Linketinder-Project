package zg.acelera.dto.like

record LikeCreateDTO(
        UUID entityLikedId,
        UUID userId
) {
    public LikeCreateDTO {
        if (!entityLikedId) throw new IllegalArgumentException("The liked entity id must be provided")
        if (!userId) throw new IllegalArgumentException("The user id must be provided")
    }
}