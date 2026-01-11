package com.example.legoassistant.repository;

import com.example.legoassistant.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findByLegoSet_IdOrderByTimestampAsc(Long legoSetId);

    List<ChatHistory> findByLegoSet_IdAndUser_UsernameOrderByTimestampAsc(Long legoSetId, String username);

    // For model context: newest first, then we can reverse in service
    List<ChatHistory> findTop10ByLegoSet_IdAndUser_UsernameOrderByTimestampDesc(Long legoSetId, String username);

    // Clear history
    @Modifying
    @Transactional
    long deleteByLegoSet_IdAndUser_Username(Long legoSetId, String username);
}
