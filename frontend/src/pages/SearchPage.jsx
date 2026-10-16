import React, { useState } from 'react';

const SearchPage = () => {
    const [searchTerm, setSearchTerm] = useState('');

    const providers = [
        {
            id: 1,
            name: 'Arafat Hossain',
            category: 'Electrician',
            location: 'Dhaka',
            rating: 5.0,
            reviewsCount: 12,
            price: 500,
            isVerified: true
        },
        {
            id: 2,
            name: 'Rahim Appliance Repairs',
            category: 'AC & Fridge Repair',
            location: 'Dhaka',
            rating: 4.8,
            reviewsCount: 8,
            price: 800,
            isVerified: true
        }
    ];

    return (
        <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif', backgroundColor: '#f8f9fa', minHeight: '100vh' }}>
            <div style={{ background: '#007bff', color: '#fff', padding: '15px 20px', borderRadius: '8px', marginBottom: '20px' }}>
                <h2>Customer Portal - Find & Compare Services</h2>
            </div>

            {/* Search Bar */}
            <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
                <input
                    type="text"
                    placeholder="Search for Electrician, Plumber, AC Repair..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    style={{ flex: 1, padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}
                />
                <button style={{ padding: '10px 20px', background: '#28a745', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                    Search
                </button>
            </div>

            <div style={{ display: 'flex', gap: '20px' }}>
                {/* Filters Sidebar */}
                <div style={{ width: '250px', background: '#fff', padding: '20px', borderRadius: '8px', height: 'fit-content', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                    <h3>Filters</h3>
                    <hr style={{ margin: '10px 0' }} />
                    <div style={{ marginBottom: '15px' }}>
                        <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '5px' }}>Category</label>
                        <select style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }}>
                            <option>All Categories</option>
                            <option>Plumbing</option>
                            <option>Electrical</option>
                            <option>Cleaning</option>
                        </select>
                    </div>
                    <div style={{ marginBottom: '15px' }}>
                        <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '5px' }}>Location</label>
                        <input type="text" placeholder="e.g. Dhaka" style={{ width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }} />
                    </div>
                    <button style={{ width: '100%', padding: '10px', background: '#007bff', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>
                        Apply Filters
                    </button>
                </div>

                {/* Provider List */}
                <div style={{ flex: 1 }}>
                    <h3>Ranked Results</h3>
                    <div style={{ marginTop: '15px' }}>
                        {providers.map((p) => (
                            <div key={p.id} style={{ background: '#fff', padding: '15px', borderRadius: '8px', marginBottom: '15px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
                                <div>
                                    <h4 style={{ margin: 0 }}>
                                        {p.name} {p.isVerified && <span style={{ background: '#e0f2fe', color: '#0369a1', padding: '2px 8px', borderRadius: '10px', fontSize: '12px', marginLeft: '8px' }}>Verified</span>}
                                    </h4>
                                    <p style={{ margin: '5px 0', color: '#666', fontSize: '14px' }}>{p.category} • {p.location}</p>
                                    <p style={{ margin: '5px 0', color: '#f59e0b', fontWeight: 'bold' }}>★ {p.rating} <span style={{ color: '#888', fontWeight: 'normal' }}>({p.reviewsCount} Reviews)</span></p>
                                    <p style={{ margin: 0 }}><b>Price:</b> ৳{p.price} / hr</p>
                                </div>
                                <div>
                                    <button style={{ padding: '8px 15px', background: '#007bff', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', marginRight: '5px' }}>Book Slot</button>
                                    <button style={{ padding: '8px 15px', background: '#6c757d', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Compare</button>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SearchPage;