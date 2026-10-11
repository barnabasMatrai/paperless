// Spiegelt die Reminder-DTOs im Backend (dueDate ist LocalDateTime: yyyy-MM-ddTHH:mm:ss)

export interface ReminderCreate {
  documentId: number;
  dueDate: string;
  description: string;
  notified: boolean;
}

export interface ReminderUpdate {
  dueDate: string;
  description: string;
  notified: boolean;
}

export interface ReminderPublic {
  id: number;
  dueDate: string;
  description: string;
  notified: boolean;
}
