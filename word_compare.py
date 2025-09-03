# korean_similarity.py

import difflib
from difflib import SequenceMatcher
import numpy as np
from typing import Optional, Union
from comparator import JaccardSimilarity, JamoSimilarity, LevenshteinSimilarity, DifflibSimilarity, NGramSimilarity

try:
    from sklearn.feature_extraction.text import TfidfVectorizer
    from sklearn.metrics.pairwise import cosine_similarity
    SKLEARN_AVAILABLE = True
except ImportError:
    SKLEARN_AVAILABLE = False
    print("Warning: scikit-learn not available. N-gram similarity will use fallback method.")

class KoreanSimilarityCalculator:
    """
    한국어 단어 유사도 계산을 위한 통합 클래스
    각 유사도 계산법을 개별 클래스로 관리
    """

    def __init__(self, similarity_threshold: float = 0.7):
        """
        초기화


        Args:
        similarity_threshold: 유사성 판단을 위한 기본 임계값
        """
        self.similarity_threshold = similarity_threshold

        # 각 유사도 계산기 초기화
        self.calculators = {
            'levenshtein': LevenshteinSimilarity(),
            'jamo': JamoSimilarity(),
        }

        self.default_weights = {
             'levenshtein': 0.2,
             'jamo': 0.8
        }

    def _calculate_all_similarities(self, word1: str, word2: str) -> dict[str, float]:
        """모든 유사도 방법으로 계산"""
        similarities = {}

        for method, calculator in self.calculators.items():
            similarities[method] = calculator.calculate(word1, word2)

        return similarities

    def _calculate_hybrid_similarity(self, word1: str, word2: str,
                                    weights: Optional[dict[str, float]] = None) -> float:
        """가중 평균을 이용한 종합 유사도 계산"""
        if weights is None:
            weights = self.default_weights

        word1 = word1.replace(" ", "")
        word2 = word2.replace(" ", "")
        similarities = self._calculate_all_similarities(word1, word2)

        # 가중 평균 계산
        weighted_score = sum(similarities[key] * weights.get(key, 0)
                             for key in similarities if key in weights)

        return weighted_score

    def find_best_match(self, target_word: str, candidate_words: list[tuple[int, int, int, int, int, int, str]]) -> dict:
        """
        주어진 단어 리스트에서 가장 유사도 점수가 높은 단어 하나를 반환

        Args:
            target_word: 찾고자 하는 대상 단어
            candidate_words: 후보 단어들의 리스트
            method: 사용할 유사도 계산 방법

        Returns:
            (best_word, best_score): 가장 유사한 단어와 그 점수
        """
        if not candidate_words:
            return None, 0.0

        best_word = None
        best_score = 0.0
        best_x, best_y = -1, -1
        best_min_x, best_min_y, best_max_x, best_max_y = -1, -1, -1, -1

        for x, y, min_x, min_y, max_x, max_y, title in candidate_words:
            score = self._calculate_hybrid_similarity(target_word, title)

            if score > best_score:
                best_score = score
                best_word = title
                best_x, best_y = x, y
                best_min_x, best_min_y, best_max_x, best_max_y = min_x, min_y, max_x, max_y

        return {
            "x": best_x,
            "y": best_y,
            "min_x": best_min_x,
            "min_y": best_min_y,
            "max_x": best_max_x,
            "max_y": best_max_y,
            "word": best_word,
            "score": best_score
        }

    def determine_page(self, need_list: list[str], page_list: list[str]) -> int:
        if not need_list:
            return 0

        candidates = [(-1, -1, -1, -1, -1, -1, title) for title in page_list]
        count = 0
        for need in need_list:
            result = self.find_best_match(need, candidates)
            score = result["score"]

            if score >= 0.7:
                count += 1

        return int(float(count) / len(need_list) >= 0.8)