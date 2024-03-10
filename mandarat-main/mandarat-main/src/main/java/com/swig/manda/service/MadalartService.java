package com.swig.manda.service;


import com.swig.manda.dto.DetailDto;
import com.swig.manda.dto.MainTopicDto;
import com.swig.manda.model.Detail;
import com.swig.manda.model.MainTopic;
import com.swig.manda.model.Member;
import com.swig.manda.repository.DetailRepository;
import com.swig.manda.repository.MainRepository;
import com.swig.manda.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MadalartService {

    private final MainRepository mainRepository;
    private final DetailRepository detailRepository;

    private final MemberRepository memberRepository;

    @Autowired
    public MadalartService(MainRepository mainRepository, DetailRepository detailRepository,MemberRepository memberRepository) {
        this.mainRepository = mainRepository;
        this.detailRepository = detailRepository;
        this.memberRepository=memberRepository;
    }

    public MainTopicDto saveMainTopics(MainTopicDto mainTopicDto) {
        // userId를 사용하여 Member 조회
        Member member = memberRepository.findByUserId(mainTopicDto.getUserId())
                .orElseThrow(() -> new RuntimeException("Member not found with userId: " + mainTopicDto.getUserId()));

        MainTopic mainTopic = new MainTopic();
        mainTopic.setTitle(mainTopicDto.getTitle());
        mainTopic.setMember(member);

        MainTopic savedMainTopic = mainRepository.save(mainTopic);

        MainTopicDto savedDto = new MainTopicDto();
        savedDto.setId(savedMainTopic.getId());
        savedDto.setTitle(savedMainTopic.getTitle());


        if (savedMainTopic.getDetails() != null) {
            List<DetailDto> detailDtos = savedMainTopic.getDetails().stream()
                    .map(this::convertDetailEntityToDto)
                    .collect(Collectors.toList());
            savedDto.setDetails(detailDtos);
        }

        savedDto.setUserId(savedMainTopic.getMember().getUserId());

        return savedDto;
    }



    public List<MainTopicDto> getAllMainTopicsByUserId(String userId) {

        List<MainTopic> mainTopics = mainRepository.findByMember_UserId(userId);


        List<MainTopicDto> mainTopicDtos = mainTopics.stream()
                .map(this::convertEntityToDto) // MainTopic 엔티티를 MainTopicDto로 변환하는 메서드
                .collect(Collectors.toList());

        return mainTopicDtos;
    }

    private MainTopicDto convertEntityToDto(MainTopic mainTopic) {
        MainTopicDto mainTopicDto = new MainTopicDto();
        mainTopicDto.setId(mainTopic.getId());
        mainTopicDto.setTitle(mainTopic.getTitle());
        mainTopicDto.setUserId(mainTopic.getMember().getUserId());
        List<DetailDto> detailDtos = mainTopic.getDetails().stream()
                .map(this::convertDetailEntityToDto)
                .collect(Collectors.toList());
        mainTopicDto.setDetails(detailDtos);


        return mainTopicDto;
    }

    public MainTopicDto getMainTopicWithSubTopicsByUserId(Long topicId, String  userId) {
        Optional<MainTopic> mainTopicOpt = mainRepository.findByIdAndMember_UserId(topicId,userId);

        if (mainTopicOpt.isPresent()) {
            MainTopic mainTopic = mainTopicOpt.get();

            List<Detail> details = detailRepository.findByMainTopicId(mainTopic.getId());

            MainTopicDto mainTopicDto = convertEntityToDto(mainTopic);

            List<DetailDto> detailDtos = details.stream()
                    .map(this::convertDetailEntityToDto)
                    .collect(Collectors.toList());
            mainTopicDto.setDetails(detailDtos);


            if (mainTopic.getMember() != null) {
                mainTopicDto.setUserId(userId);
            }

            return mainTopicDto;
        } else {
            // 메인 토픽이 존재하지 않을 경우 null 반환
            return null;
        }
    }


    private DetailDto convertDetailEntityToDto(Detail detail) {
        DetailDto detailDto = new DetailDto();
        detailDto.setId(detail.getId());
        detailDto.setContent(detail.getContent());
        detailDto.setMainTopicId(detail.getMainTopic().getId());
        return detailDto;


    }



    public  DetailDto saveDetail(DetailDto detailDto) {

        MainTopic mainTopic = mainRepository.findById(detailDto.getMainTopicId())
                .orElseThrow(() -> new RuntimeException("MainTopic not found with id: " + detailDto.getMainTopicId()));


        Detail detail = new Detail();
        detail.setContent(detailDto.getContent());
        detail.setMainTopic(mainTopic); // 여기서 MainTopic 설정


        Detail savedDetail = detailRepository.save(detail);


        DetailDto savedDetailDto = new DetailDto();
        savedDetailDto.setId(savedDetail.getId());
        savedDetailDto.setContent(savedDetail.getContent());
        savedDetailDto.setMainTopicId(savedDetail.getMainTopic().getId());

        return savedDetailDto;
    }
    public void deleteAllMainTopics() {
        mainRepository.deleteAll();
    }

    public void deleteAlldetail(){
        detailRepository.deleteAll();
    }


    public MainTopicDto updateMainTopic(Long topicId, MainTopicDto mainTopicDto) {
        Optional<MainTopic> mainTopicOptional = mainRepository.findById(topicId);
        if (mainTopicOptional.isPresent()) {
            MainTopic mainTopic = mainTopicOptional.get();
            mainTopic.setTitle(mainTopicDto.getTitle());


            Member member = memberRepository.findByUserId(mainTopicDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("응? id를 찾지 못했습니다. " + mainTopicDto.getUserId()));

            mainTopic.setMember(member);


            mainRepository.save(mainTopic);


            MainTopicDto savedDto = new MainTopicDto();
            savedDto.setId(mainTopic.getId());
            savedDto.setTitle(mainTopic.getTitle());
            savedDto.setUserId(member.getUserId());

            return savedDto;
        } else {
            return null;
        }
    }


    public DetailDto updateDetail(Long detailId, DetailDto detailDto) {
        Optional<Detail> detailOptional = detailRepository.findById(detailId);
        if (detailOptional.isPresent()) {
            Detail detail = detailOptional.get();

            detail.setContent(detailDto.getContent());

            detailRepository.save(detail);

            DetailDto savedDto = new DetailDto();
            savedDto.setId(detail.getId());
            savedDto.setContent(detail.getContent());

            savedDto.setMainTopicId(detail.getMainTopic().getId());

            return savedDto;
        } else {
            return null;
        }
    }





}

