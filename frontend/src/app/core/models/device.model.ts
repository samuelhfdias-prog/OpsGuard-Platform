export type DeviceType = 'SMARTPHONE' | 'TABLET' | 'LAPTOP' | 'TRACKER' | 'OTHER';

export interface Device {
  id: number;
  name: string;
  serialNumber: string;
  type: DeviceType;
  organizationId: number;
  organizationName: string;
  createdAt: string;
  updatedAt: string;
}

export interface DeviceRequest {
  name: string;
  serialNumber: string;
  type: DeviceType;
  organizationId: number;
}

export const DEVICE_TYPE_LABELS: Record<DeviceType, string> = {
  SMARTPHONE: 'Smartphone',
  TABLET: 'Tablet',
  LAPTOP: 'Notebook',
  TRACKER: 'Rastreador',
  OTHER: 'Outro'
};
