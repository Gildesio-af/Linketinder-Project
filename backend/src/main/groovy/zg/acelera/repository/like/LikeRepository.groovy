package zg.acelera.repository.like

interface LikeRepository {
    void like(UUID entityLikedId, UUID userId)
    void unlike(UUID entityLikedId, UUID userId)
    boolean existsLike(UUID entityLikedId, UUID userId)
}