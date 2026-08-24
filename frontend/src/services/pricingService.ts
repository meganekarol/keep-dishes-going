import { api } from './apiClient';
import type {
  AdjustPriceRangeCriteriaRequest,
  CriteriaVersion,
  DemoHistoryResponse,
  PriceRangeEvolution,
} from '../types/api';

export const pricingService = {
  /** Open to everyone: the graph is an extra service for customers (US-26). */
  evolutionOf: (restaurantId: string) =>
    api.get<PriceRangeEvolution>(`/api/restaurants/${restaurantId}/price-range-evolution`),

  /** Admin only (US-32). */
  criteriaTimeline: () => api.get<CriteriaVersion[]>('/api/price-range-criteria'),

  /**
   * Appends a version rather than replacing the thresholds, so older versions
   * keep governing their own periods. Hence POST, never PUT.
   */
  adjustCriteria: (request: AdjustPriceRangeCriteriaRequest) =>
    api.post<void>('/api/price-range-criteria', request),

  /** Admin only. Back-dated sample history, so the graph has a year to draw. */
  seedDemoHistory: () => api.post<DemoHistoryResponse>('/api/pricing/demo-history'),
};