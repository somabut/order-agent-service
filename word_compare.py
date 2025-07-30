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
            'difflib': DifflibSimilarity(),
            'jamo': JamoSimilarity(),
            'ngram': NGramSimilarity(),
            'jaccard': JaccardSimilarity()
        }

        # 종합 유사도 계산용 가중치
        self.default_weights = {
            'levenshtein': 0.2,
            'difflib': 0.175,
            'ngram': 0.2,
            'jaccard': 0.175,
            'jamo': 0.25
        }

    def calculate_all_similarities(self, word1: str, word2: str) -> dict[str, float]:
        """모든 유사도 방법으로 계산"""
        similarities = {}

        for method, calculator in self.calculators.items():
            similarities[method] = calculator.calculate(word1, word2)

        return similarities

    def calculate_hybrid_similarity(self, word1: str, word2: str,
                                    weights: Optional[dict[str, float]] = None) -> float:
        """가중 평균을 이용한 종합 유사도 계산"""
        if weights is None:
            weights = self.default_weights

        similarities = self.calculate_all_similarities(word1, word2)

        # 가중 평균 계산
        weighted_score = sum(similarities[key] * weights.get(key, 0)
                             for key in similarities if key in weights)

        return weighted_score

    def find_best_match(self, target_word: str, candidate_words: list[tuple[int, int, str]]) -> dict:
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

        for x, y, title in candidate_words:
            score = self.calculate_hybrid_similarity(target_word, title)

            if score > best_score:
                best_score = score
                best_word = title
                best_x, best_y = x, y

        return {
            "x": best_x,
            "y": best_y,
            "word": best_word,
            "score": best_score
        }
        return best_x, best_y, best_word, best_score