import type { Meta, StoryObj } from "@storybook/react";
import { DataTable, type Column } from "./data-table";

/**
 * DataTable component stories.
 * Validates: P3.11.3 — Component variants catalog
 */

interface SampleRow {
  id: string;
  name: string;
  email: string;
  status: "ACTIVE" | "INACTIVE";
  createdAt: string;
}

const sampleData = {
  content: [
    {
      id: "1",
      name: "Acme Corp",
      email: "info@acme.com",
      status: "ACTIVE" as const,
      createdAt: "2025-01-15",
    },
    {
      id: "2",
      name: "Globex Inc",
      email: "hello@globex.com",
      status: "INACTIVE" as const,
      createdAt: "2025-01-16",
    },
    {
      id: "3",
      name: "Initech LLC",
      email: "contact@initech.com",
      status: "ACTIVE" as const,
      createdAt: "2025-01-17",
    },
  ],
  page: { number: 0, size: 20, totalElements: 3, totalPages: 1 },
};

const columns: Column<SampleRow>[] = [
  { key: "name", header: "Name" },
  { key: "email", header: "Email" },
  {
    key: "status",
    header: "Status",
    render: (row) => (
      <span
        className={`rounded-full px-2 py-0.5 text-xs font-medium ${
          row.status === "ACTIVE" ? "bg-green-100 text-green-800" : "bg-gray-100 text-gray-700"
        }`}
      >
        {row.status}
      </span>
    ),
  },
  { key: "createdAt", header: "Created" },
];

const meta: Meta<typeof DataTable<SampleRow>> = {
  title: "Shared/DataTable",
  component: DataTable,
  parameters: {
    layout: "padded",
    docs: {
      description: { component: "Generic server-side paginated data table with skeleton states." },
    },
  },
  tags: ["autodocs"],
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: { columns, data: sampleData, loading: false },
};

export const Loading: Story = {
  args: { columns, data: null, loading: true },
};

export const Empty: Story = {
  args: {
    columns,
    data: { content: [], page: { number: 0, size: 20, totalElements: 0, totalPages: 0 } },
    loading: false,
  },
};

export const WithError: Story = {
  args: { columns, data: null, loading: false, error: "Failed to load data. Please try again." },
};

export const WithPagination: Story = {
  args: {
    columns,
    data: {
      content: sampleData.content,
      page: { number: 2, size: 20, totalElements: 150, totalPages: 8 },
    },
    loading: false,
    page: 2,
  },
};
