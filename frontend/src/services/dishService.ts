import { api } from './apiClient';
import type {
  CreateDishRequest,
  CreateDishResponse,
  EditDishRequest,
  EditDishResponse,
  OwnerMenu,
} from '../types/api';

/**
 * The owner's dish endpoints sit at `/api/{restaurantId}/dishes`, with no
 * `restaurants` segment. That is the backend's mapping, not a typo here: the
 * public menu is the one under `/api/restaurants/{id}/dishes`.
 */
const dishesPath = (restaurantId: string) => `/api/${restaurantId}/dishes`;

export const dishService = {
  /** Every dish the owner has, live and draft versions alike (US-03). */
  getOwnerMenu: (restaurantId: string) => api.get<OwnerMenu>(dishesPath(restaurantId)),

  /** Add a dish. It starts as a draft, so customers do not see it yet (US-03). */
  create: (restaurantId: string, request: CreateDishRequest) =>
    api.post<CreateDishResponse>(`${dishesPath(restaurantId)}/new`, request),

  /**
   * Park an edit as a draft (US-03).
   *
   * The live version keeps serving customers until the draft is published, so
   * this is safe to call on a dish people are ordering.
   */
  editDraft: (restaurantId: string, dishId: string, request: EditDishRequest) =>
    api.put<EditDishResponse>(`${dishesPath(restaurantId)}/${dishId}/draft`, request),

  /**
   * Put the draft live (US-04).
   *
   * Rejected when the restaurant already has its maximum number of published
   * dishes, or when the dish has no draft to publish.
   */
  publish: (restaurantId: string, dishId: string) =>
    api.post<void>(`${dishesPath(restaurantId)}/${dishId}/publish`),

  /** Take a dish off the menu (US-05). Rejected while it has a draft. */
  unpublish: (restaurantId: string, dishId: string) =>
    api.post<void>(`${dishesPath(restaurantId)}/${dishId}/unpublish`),

  /** Sold out for now, but staying on the menu (US-08). */
  markOutOfStock: (restaurantId: string, dishId: string) =>
    api.post<void>(`${dishesPath(restaurantId)}/${dishId}/mark-out-of-stock`),

  /** Back in stock (US-08). */
  markInStock: (restaurantId: string, dishId: string) =>
    api.post<void>(`${dishesPath(restaurantId)}/${dishId}/mark-in-stock`),

  /** Publish every pending draft at once (US-06). */
  applyChanges: (restaurantId: string) =>
    api.post<void>(`${dishesPath(restaurantId)}/apply-changes`),

  /**
   * Publish every pending draft later instead (US-07).
   *
   * @param scheduledTime a local date and time with no zone, as the backend
   *                      reads it, e.g. `2026-08-20T18:30:00`. Must be future.
   */
  scheduleChanges: (restaurantId: string, scheduledTime: string) =>
    api.post<void>(
      `${dishesPath(restaurantId)}/schedule-changes?scheduledTime=${encodeURIComponent(scheduledTime)}`,
    ),
};